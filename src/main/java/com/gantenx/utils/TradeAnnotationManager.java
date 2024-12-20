package com.gantenx.utils;

import com.gantenx.model.Order;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.TextAnchor;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TradeAnnotationManager {
    private static final int FONT_SIZE = 10;
    private static final float LINE_WIDTH = 1.0f;
    private static final double BASE_Y_POSITION = 530;
    private static final double TEXT_Y_SPACING = 20;
    private static final Color BUY_COLOR = new Color(0, 150, 0);
    private static final Color SELL_COLOR = new Color(150, 0, 0);
    private static final Font ANNOTATION_FONT = new Font("SansSerif", Font.BOLD, FONT_SIZE);

    private final XYPlot mainPlot;
    private final XYPlot rsiPlot;
    private final Map<Long, List<XYTextAnnotation>> annotationMap = new HashMap<>();

    public TradeAnnotationManager(XYPlot mainPlot, XYPlot rsiPlot) {
        this.mainPlot = mainPlot;
        this.rsiPlot = rsiPlot;
    }

    public void addTradeMarkers(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        // 按时间戳分组
        Map<Long, List<Order>> ordersByTimestamp = orders.stream()
                .collect(Collectors.groupingBy(Order::getTimestamp));

        // 处理每个时间戳的订单组
        ordersByTimestamp.forEach(this::processOrderGroup);
    }

    private void processOrderGroup(Long timestamp, List<Order> orders) {
        // 添加垂直线
        addVerticalLine(timestamp, orders.size());

        // 对订单按符号分组
        Map<String, List<Order>> ordersBySymbol = orders.stream()
                .collect(Collectors.groupingBy(Order::getSymbol));

        // 计算每个符号的位置
        int symbolIndex = 0;
        for (Map.Entry<String, List<Order>> entry : ordersBySymbol.entrySet()) {
            String symbol = entry.getKey();
            List<Order> symbolOrders = entry.getValue();

            // 处理同一符号的订单
            processSymbolOrders(timestamp, symbol, symbolOrders, symbolIndex);
            symbolIndex += symbolOrders.size();
        }
    }

    private void addVerticalLine(Long timestamp, int orderCount) {
        XYLineAnnotation line = new XYLineAnnotation(
                timestamp,
                0,
                timestamp,
                BASE_Y_POSITION + orderCount * TEXT_Y_SPACING,
                createDashedStroke(),
                Color.GRAY
        );

        mainPlot.addAnnotation(line);
        rsiPlot.addAnnotation(line);
    }

    private void processSymbolOrders(Long timestamp, String symbol, List<Order> orders, int startIndex) {
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            addOrderAnnotation(order, timestamp, startIndex + i);
        }
    }


    private double adjustPositionForOverlap(Long timestamp, double basePosition) {
        double adjustedPosition = basePosition;
        List<XYTextAnnotation> existingAnnotations = annotationMap.getOrDefault(timestamp, Collections.emptyList());

        boolean overlap;
        do {
            overlap = false;
            for (XYTextAnnotation existing : existingAnnotations) {
                if (Math.abs(existing.getY() - adjustedPosition) < TEXT_Y_SPACING) {
                    adjustedPosition -= TEXT_Y_SPACING;
                    overlap = true;
                    break;
                }
            }
        } while (overlap);

        return adjustedPosition;
    }

    private String formatOrderInfo(Order order) {
        // 将订单信息分成两行
        return String.format("%s %s \n @%.2f",
                order.getSymbol(),
                order.getType().toUpperCase(),
                order.getPrice());
    }

    private void addOrderAnnotation(Order order, Long timestamp, int index) {
        Color orderColor = order.getType().equalsIgnoreCase("buy") ? BUY_COLOR : SELL_COLOR;
        String orderInfo = formatOrderInfo(order);
        double yPosition = calculateYPosition(timestamp, index);

        // 为每行文本创建单独的标注
        String[] lines = orderInfo.split("\n");
        double lineHeight = 15.0; // 行间距

        for (int i = 0; i < lines.length; i++) {
            double lineYPosition = yPosition - (i * lineHeight);
            XYTextAnnotation annotation = createTextAnnotation(
                    lines[i].trim(),
                    timestamp,
                    lineYPosition,
                    orderColor
            );

            // 存储标注
            annotationMap.computeIfAbsent(timestamp, k -> new ArrayList<>()).add(annotation);
            mainPlot.addAnnotation(annotation);
        }
    }

    private XYTextAnnotation createTextAnnotation(String text, Long timestamp, double yPosition, Color color) {
        XYTextAnnotation annotation = new XYTextAnnotation(text, timestamp, yPosition);

        annotation.setFont(ANNOTATION_FONT);
        annotation.setTextAnchor(TextAnchor.BOTTOM_CENTER);
        annotation.setPaint(color);

        // 调整背景以适应单行文本
        annotation.setBackgroundPaint(new Color(255, 255, 255, 200));
        annotation.setOutlinePaint(color);
        annotation.setOutlineVisible(true);

        return annotation;
    }

    // 调整计算位置的方法以考虑多行文本
    private double calculateYPosition(Long timestamp, int index) {
        // 增加行间距的考虑
        double lineSpacing = 20.0; // 每组订单之间的间距
        double basePosition = BASE_Y_POSITION - (index * (TEXT_Y_SPACING + lineSpacing));
        return adjustPositionForOverlap(timestamp, basePosition);
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

    public static void markOrders(XYPlot mainPlot, XYPlot rsiPlot, List<Order> orderMap) {
        TradeAnnotationManager annotationManager = new TradeAnnotationManager(mainPlot, rsiPlot);
        annotationManager.addTradeMarkers(orderMap);
    }
}