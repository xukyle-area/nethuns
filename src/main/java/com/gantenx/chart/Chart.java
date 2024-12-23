package com.gantenx.chart;

import com.gantenx.constant.Series;
import com.gantenx.engine.Order;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.*;

public class Chart extends ApplicationFrame {
    private final JFreeChart combinedChart;

    protected Chart(Map<Series, Map<Long, Double>> klineMap, XYPlot subPlot, List<Order> orderList) {
        super("Trading Line");
        XYPlot mainPlot = ChartUtils.createMainPlot(klineMap);
        OrderMarker.markOrders(mainPlot, subPlot, orderList);
        CombinedDomainXYPlot combinedPlot = ChartUtils.setCombinedPlot(mainPlot, subPlot);
        combinedChart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        this.setupChartPanel();
    }

    private void setupChartPanel() {
        ChartPanel chartPanel = new ChartPanel(combinedChart);
        chartPanel.setPreferredSize(new Dimension(CHART_WIDTH, CHART_HEIGHT));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setMouseZoomable(true);
        setContentPane(chartPanel);
    }

    public JFreeChart getCombinedChart() {
        return combinedChart;
    }
}
