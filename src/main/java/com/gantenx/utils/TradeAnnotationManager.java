package com.gantenx.utils;

import com.gantenx.model.Order;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.TextAnchor;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.gantenx.utils.DateUtils.SIMPLE_DATE_FORMAT_WITHOUT_TIME;

public class TradeAnnotationManager {
    private static final int FONT_SIZE = 10;
    private static final float LINE_WIDTH = 1.0f;
    private static final double BASE_Y_POSITION = 530;
    private static final double TEXT_Y_SPACING = 20;
    private static final double LINE_LENGTH = 530; // 竖直线长度
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

    public static void markOrders(XYPlot mainPlot, XYPlot rsiPlot, List<Order> orderMap) {
        TradeAnnotationManager annotationManager = new TradeAnnotationManager(mainPlot, rsiPlot);
        annotationManager.addTradeMarkers(orderMap);
    }

    public void addTradeMarkers(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Map<Long, List<Order>> ordersByTimestamp = orders.stream()
                .collect(Collectors.groupingBy(Order::getTimestamp));

        List<Long> sortedTimestamps = new ArrayList<>(ordersByTimestamp.keySet());
        Collections.sort(sortedTimestamps);

        sortedTimestamps.forEach(timestamp -> {
            processOrderGroup(timestamp, ordersByTimestamp.get(timestamp));
            isTopPosition = !isTopPosition;
        });
    }


    private void processSymbolOrders(Long timestamp, List<Order> orders, int startIndex) {
        for (int i = 0; i < orders.size(); i++) {
            addOrderAnnotation(orders.get(i), timestamp, startIndex + i);
        }
    }

    private void addOrderAnnotation(Order order, Long timestamp, int index) {
        Color orderColor = order.getType().equalsIgnoreCase("buy") ? BUY_COLOR : SELL_COLOR;
        String[] lines = formatOrderInfo(order).split("\n");
        double baseY = calculateYPosition(index);

        // 添加每一行文本
        for (int i = 0; i < lines.length; i++) {
            double yPos = baseY + (i * 10.0); // 行间距为15
            XYTextAnnotation annotation = new XYTextAnnotation(lines[i].trim(), timestamp, yPos);
            styleAnnotation(annotation, orderColor);
            mainPlot.addAnnotation(annotation);
            annotationMap.computeIfAbsent(timestamp, k -> new ArrayList<>()).add(annotation);
        }
    }

    private String formatOrderInfo(Order order) {
        return String.format("%s %s\n@%.2f",
                order.getSymbol(),
                order.getType().toUpperCase(),
                order.getPrice());
    }

    private double calculateYPosition(int index) {
        if (isTopPosition) {
            return BASE_Y_POSITION - (index * TEXT_Y_SPACING);
        } else {
            return TEXT_Y_SPACING + (index * TEXT_Y_SPACING);
        }
    }

    private void styleAnnotation(XYTextAnnotation annotation, Color color) {
        annotation.setFont(ANNOTATION_FONT);
        annotation.setTextAnchor(TextAnchor.BOTTOM_CENTER);
        annotation.setPaint(color);
        annotation.setBackgroundPaint(new Color(255, 255, 255, 200));
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


    // 添加时间标记的常量
    private static final int TIME_FONT_SIZE = 9;
    private static final Font TIME_FONT = new Font("SansSerif", Font.PLAIN, TIME_FONT_SIZE);
    private static final double TIME_Y_OFFSET = 10.0; // 时间标记距离底部的距离

    // ... 其他代码保持不变，直到 processOrderGroup 方法 ...

    private void processOrderGroup(Long timestamp, List<Order> orders) {
        // 添加竖直线
        XYLineAnnotation line = new XYLineAnnotation(
                timestamp, 0,
                timestamp, LINE_LENGTH,
                createDashedStroke(),
                Color.GRAY
        );
        mainPlot.addAnnotation(line);
        rsiPlot.addAnnotation(line);

        // 添加时间标记
        addTimeAnnotation(timestamp);

        // 处理订单
        Map<String, List<Order>> ordersBySymbol = orders.stream()
                .collect(Collectors.groupingBy(Order::getSymbol));

        int symbolIndex = 0;
        for (Map.Entry<String, List<Order>> entry : ordersBySymbol.entrySet()) {
            List<Order> symbolOrders = entry.getValue();
            processSymbolOrders(timestamp, symbolOrders, symbolIndex);
            symbolIndex += symbolOrders.size();
        }
    }

    private void addTimeAnnotation(Long timestamp) {
        // 格式化时间
        String timeStr = SIMPLE_DATE_FORMAT_WITHOUT_TIME.format(new Date(timestamp));

        // 创建时间标注
        XYTextAnnotation timeAnnotation = new XYTextAnnotation(
                timeStr,
                timestamp,
                TIME_Y_OFFSET  // 在底部显示时间
        );

        // 设置时间标注样式
        styleTimeAnnotation(timeAnnotation);

        // 添加到主图和RSI图
        mainPlot.addAnnotation(timeAnnotation);
        rsiPlot.addAnnotation(timeAnnotation);
    }

    private void styleTimeAnnotation(XYTextAnnotation annotation) {
        annotation.setFont(TIME_FONT);
        annotation.setTextAnchor(TextAnchor.TOP_CENTER); // 文本在线的上方
        annotation.setPaint(Color.DARK_GRAY);
        annotation.setBackgroundPaint(new Color(255, 255, 255, 200));
        annotation.setOutlinePaint(Color.GRAY);
        annotation.setOutlineVisible(true);
    }
}