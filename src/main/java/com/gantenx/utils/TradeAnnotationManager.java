package com.gantenx.utils;

import com.gantenx.engine.Order;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.TextAnchor;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.gantenx.constant.Side.BUY;

public class TradeAnnotationManager<T> {
    private static final int FONT_SIZE = 10;
    private static final float LINE_WIDTH = 1.0f;
    private static final Color BUY_COLOR = new Color(0, 150, 0);
    private static final Color SELL_COLOR = new Color(150, 0, 0);
    private static final Font ANNOTATION_FONT = new Font("SansSerif", Font.BOLD, FONT_SIZE);

    private final XYPlot mainPlot;
    private final XYPlot rsiPlot;
    private final Map<Long, List<XYTextAnnotation>> annotationMap = new HashMap<>();
    private boolean isTopPosition = true;

    public TradeAnnotationManager(XYPlot mainPlot, XYPlot rsiPlot) {
        this.mainPlot = mainPlot;
        this.rsiPlot = rsiPlot;
    }

    public static <T> void markOrders(XYPlot mainPlot, XYPlot rsiPlot, List<Order<T>> orderMap) {
        TradeAnnotationManager<T> annotationManager = new TradeAnnotationManager<>(mainPlot, rsiPlot);
        annotationManager.addTradeMarkers(orderMap);
    }

    public void addTradeMarkers(List<Order<T>> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Map<Long, List<Order<T>>> ordersByTimestamp = orders.stream()
                .collect(Collectors.groupingBy(Order::getTimestamp));

        List<Long> sortedTimestamps = new ArrayList<>(ordersByTimestamp.keySet());
        Collections.sort(sortedTimestamps);

        sortedTimestamps.forEach(timestamp -> {
            processOrderGroup(timestamp, ordersByTimestamp.get(timestamp));
            isTopPosition = !isTopPosition;
        });
    }


    private void processSymbolOrders(Long timestamp, List<Order<T>> orders, int startIndex) {
        for (int i = 0; i < orders.size(); i++) {
            addOrderAnnotation(orders.get(i), timestamp, startIndex + i);
        }
    }

    private void addOrderAnnotation(Order<T> order, Long timestamp, int index) {
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

    private String formatOrderInfo(Order order) {
        return String.format("%s %s\n@%.2f",
                order.getSymbol(),
                order.getType().name(),
                order.getPrice());
    }

    private double calculateYPosition(int index) {
        double yLower = mainPlot.getRangeAxis().getLowerBound();
        double yUpper = mainPlot.getRangeAxis().getUpperBound();
        double range = yUpper - yLower;

        // 动态计算基础 Y 坐标
        double baseY = isTopPosition ? yUpper - (0.05 * range) : yLower + (0.05 * range); // 10% 的上方或下方偏移

        // 动态计算间距
        double spacing = 0.03 * range; // 3% 的范围作为间距

        // 根据 index 动态调整 Y 坐标
        return baseY - (index * spacing);
    }


    private void styleAnnotation(XYTextAnnotation annotation, Color color) {
        annotation.setFont(ANNOTATION_FONT);
        annotation.setTextAnchor(TextAnchor.BOTTOM_CENTER);
        annotation.setPaint(color);

        // 根据轴范围调整背景透明度（例如固定透明度的 80%）
        annotation.setBackgroundPaint(new Color(255, 255, 255, (int) (0.8 * 255)));

        // 动态调整边框宽度（例如字体高度的 10%）
        annotation.setOutlineStroke(new BasicStroke((float) FONT_SIZE * 0.1f));
        annotation.setOutlinePaint(color);
        annotation.setOutlineVisible(true);
    }


    private BasicStroke createDashedStroke() {
        return new BasicStroke(
                LINE_WIDTH,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f,
                new float[]{10.0f},
                0.0f
        );
    }


    private void processOrderGroup(Long timestamp, List<Order<T>> orders) {
        // 添加竖直线
        XYLineAnnotation line = new XYLineAnnotation(timestamp, 0,
                                                     timestamp, 1000_000,
                createDashedStroke(),
                Color.GRAY
        );
        mainPlot.addAnnotation(line);
        rsiPlot.addAnnotation(line);
        // this.addOrderTag(timestamp, orders);
    }

    private void addOrderTag(Long timestamp, List<Order<T>> orders) {
        // 处理订单
        Map<T, List<Order<T>>> ordersBySymbol = orders.stream()
                .collect(Collectors.groupingBy(Order::getSymbol));

        int symbolIndex = 0;
        for (Map.Entry<T, List<Order<T>>> entry : ordersBySymbol.entrySet()) {
            List<Order<T>> symbolOrders = entry.getValue();
            processSymbolOrders(timestamp, symbolOrders, symbolIndex);
            symbolIndex += symbolOrders.size();
        }
    }
}