package com.gantenx.util;

import com.gantenx.model.Order;
import com.gantenx.model.RSI;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.util.List;

public class TradingChart extends ApplicationFrame {

    public TradingChart(String title, List<RSI> rsiList, List<Order> orderList) {
        super(title);
        XYSeries rsiSeries = new XYSeries("RSI");
        for (RSI rsi : rsiList) {
            rsiSeries.add(rsi.getOpenTime(), rsi.getRsi());
        }

        // Create the K-line (price) series
        XYSeries kLineSeries = new XYSeries("K Line");
        for (RSI rsi : rsiList) {
            kLineSeries.add(rsi.getOpenTime(), Double.parseDouble(rsi.getClosePrice()));
        }

        // Create the series collection
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(kLineSeries);
        dataset.addSeries(rsiSeries);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Time",
                "Price",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Customize the chart to add order markers
        XYPlot plot = chart.getXYPlot();
        for (Order order : orderList) {
            // Place markers for buy/sell orders
            Color color = order.getType().equals("buy") ? Color.GREEN : Color.RED;
            XYPointerAnnotation xyPointerAnnotation = new XYPointerAnnotation(
                    order.getType(),
                    order.getTimestamp(),
                    order.getPrice(),
                    0.0
            );
            xyPointerAnnotation.setPaint(color);
            plot.addAnnotation(xyPointerAnnotation);
        }

        // Set chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 600));
        setContentPane(panel);
    }
}