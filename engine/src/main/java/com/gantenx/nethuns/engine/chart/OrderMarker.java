package com.gantenx.nethuns.engine.chart;

import static com.gantenx.nethuns.commons.enums.Side.BUY;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.util.stream.Collectors;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.TextAnchor;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.utils.CollectionUtils;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.engine.model.Order;


public class OrderMarker {
    private static final int FONT_SIZE = 10;
    private static final float LINE_WIDTH = 0.5f;
    private static final Color BUY_COLOR = new Color(0, 150, 0); // 绿色
    private static final Color SELL_COLOR = new Color(150, 0, 0); // 红色
    private static final Font ANNOTATION_FONT = new Font("SansSerif", Font.BOLD, FONT_SIZE);

    private final XYPlot mainPlot;
    private final XYPlot subPlot;
    private final Map<Long, List<XYTextAnnotation>> annotationMap = new HashMap<>();
    private int timestampId = 0;

    public OrderMarker(XYPlot mainPlot, XYPlot subPlot) {
        this.mainPlot = mainPlot;
        this.subPlot = subPlot;
    }

    public static void markOrders(XYPlot mainPlot, XYPlot subPlot, List<Order> orders) {
        OrderMarker annotationManager = new OrderMarker(mainPlot, subPlot);
        annotationManager.addTradeMarkers(orders);
    }

    public void addTradeMarkers(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Map<Long, List<Order>> ordersByTimestamp = orders.stream().collect(Collectors.groupingBy(Order::getTimestamp));

        List<Long> sortedTimestamps = CollectionUtils.getTimestamps(ordersByTimestamp);

        sortedTimestamps.forEach(timestamp -> {
            timestampId++;
            this.processOrderGroup(timestamp, ordersByTimestamp.get(timestamp));
        });
    }

    private void processOrderGroup(Long timestamp, List<Order> orders) {
        // 获取订单类型颜色
        boolean isBuyOrder = orders.stream().anyMatch(order -> order.getType().equals(BUY));
        Color lineColor = isBuyOrder ? BUY_COLOR : SELL_COLOR;

        // 添加竖直线到 mainPlot
        XYLineAnnotation mainLine = new XYLineAnnotation(timestamp, mainPlot.getRangeAxis().getLowerBound(), timestamp,
                mainPlot.getRangeAxis().getUpperBound(), createDashedStroke(), lineColor);
        mainPlot.addAnnotation(mainLine);

        // 添加竖直线到 rsiPlot
        if (Objects.nonNull(subPlot)) {
            XYLineAnnotation rsiLine = new XYLineAnnotation(timestamp, subPlot.getRangeAxis().getLowerBound(),
                    timestamp, subPlot.getRangeAxis().getUpperBound(), createDashedStroke(), lineColor);
            subPlot.addAnnotation(rsiLine);
        }

        // 添加订单标签
        addOrderTag(timestamp, orders);
    }

    private BasicStroke createDashedStroke() {
        return new BasicStroke(LINE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f},
                0.0f);
    }

    private void addOrderTag(Long timestamp, List<Order> orders) {
        // 处理订单
        Map<Symbol, List<Order>> ordersBySymbol = orders.stream().collect(Collectors.groupingBy(Order::getSymbol));

        int symbolIndex = 0;
        for (Map.Entry<Symbol, List<Order>> entry : ordersBySymbol.entrySet()) {
            List<Order> symbolOrders = entry.getValue();
            processSymbolOrders(timestamp, symbolOrders, symbolIndex);
            symbolIndex += symbolOrders.size();
        }
    }

    private void processSymbolOrders(Long timestamp, List<Order> orders, int startIndex) {
        for (int i = 0; i < orders.size(); i++) {
            addOrderAnnotation(orders.get(i), timestamp, startIndex + i);
        }
    }


    private void addOrderAnnotation(Order order, Long timestamp, int index) {
        Color orderColor = order.getType().equals(BUY) ? BUY_COLOR : SELL_COLOR;
        String[] lines = formatOrderInfo(order).split("\n");

        // 获取基础 Y 坐标（动态位置）
        double baseY = calculateYPosition(index);

        // 为多行文本添加间距，避免重叠
        double lineSpacing = mainPlot.getRangeAxis().getRange().getLength() * 0.02; // 行间距占 Y 轴范围的 2%

        // 添加每一行文本
        for (int i = 0; i < lines.length; i++) {
            double yPos = baseY - (i * lineSpacing); // 每行下移固定间距
            XYTextAnnotation annotation = new XYTextAnnotation(lines[i].trim(), timestamp, yPos);
            styleAnnotation(annotation, orderColor);
            mainPlot.addAnnotation(annotation);

            // 存储标注到 annotationMap
            annotationMap.computeIfAbsent(timestamp, k -> new ArrayList<>()).add(annotation);
        }
    }

    private double calculateYPosition(int index) {
        double yLower = mainPlot.getRangeAxis().getLowerBound();
        double yUpper = mainPlot.getRangeAxis().getUpperBound();
        double range = yUpper - yLower;

        boolean isTopPosition = timestampId % 2 == 0;
        int tiers = timestampId % 3 + 1;
        // 动态计算基础 Y 坐标
        double baseY = isTopPosition ? yUpper - (tiers * 0.05 * range) : yLower + (tiers * 0.05 * range); // 10%
                                                                                                          // 的上方或下方偏移

        // 动态计算间距
        double spacing = 0.03 * range; // 3% 的范围作为间距

        // 根据 index 动态调整 Y 坐标
        return baseY - (index * spacing);
    }

    private String formatOrderInfo(Order order) {
        return String.format("%s %s\n%s@%.2f", order.getSymbol(), DateUtils.getDateForOrderMarker(order.getTimestamp()),
                order.getType().name(), order.getPrice());
    }

    private void styleAnnotation(XYTextAnnotation annotation, Color color) {
        annotation.setFont(ANNOTATION_FONT);
        annotation.setTextAnchor(TextAnchor.BOTTOM_CENTER);
        annotation.setPaint(color);
        annotation.setBackgroundPaint(new Color(255, 255, 255, 200));
        annotation.setOutlineStroke(new BasicStroke((float) FONT_SIZE * 0.1f));
        annotation.setOutlinePaint(color);
    }
}
