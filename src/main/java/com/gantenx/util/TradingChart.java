package com.gantenx.util;

import com.gantenx.model.Order;
import com.gantenx.model.RSI;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class TradingChart extends ApplicationFrame {

    public TradingChart(String title, List<RSI> rsiList, List<Order> orderList) {
        super(title);

        // Create the RSI series
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

        // Create the RSI dataset
        XYSeriesCollection rsiDataset = new XYSeriesCollection();
        rsiDataset.addSeries(rsiSeries);

        // Create the chart for K-Line
        JFreeChart chart = ChartFactory.createXYLineChart(
                "K-Line and RSI Chart",
                "Time",
                "Price",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Get the plot and configure the time axis
        XYPlot plot = chart.getXYPlot();

        // Create a DateAxis for the time axis
        DateAxis timeAxis = new DateAxis("Time");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timeAxis.setDateFormatOverride(format);
        plot.setDomainPannable(true); // Make it pannable
        plot.setDomainCrosshairVisible(true);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        plot.setDomainAxis(timeAxis);

        // Create and add the secondary axis for RSI using NumberAxis
        NumberAxis rsiAxis = new NumberAxis("RSI");
        rsiAxis.setRange(0.0, 600.0);  // RSI range between 0 and 100
        plot.setRangeAxis(1, rsiAxis);
        plot.setDataset(1, rsiDataset);
        plot.mapDatasetToRangeAxis(1, 1); // Map RSI dataset to the second Y axis

        // Customize the chart to add order markers
        for (Order order : orderList) {
            // Place markers for buy/sell orders
            Color color = order.getType().equals("buy") ? Color.GREEN : Color.RED;
            XYPointerAnnotation xyPointerAnnotation = new XYPointerAnnotation(
                    order.getType() + ":" + order.getPrice(),
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