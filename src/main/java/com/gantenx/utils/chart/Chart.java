package com.gantenx.utils.chart;

import com.gantenx.engine.Order;
import com.gantenx.utils.CollectionUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.util.List;

import static com.gantenx.constant.Constants.*;

public class Chart extends ApplicationFrame {
    private final JFreeChart combinedChart;

    protected Chart(XYPlot mainPlot, XYPlot subPlot, List<Order> orderList) {
        super("Trading Line");
        if (!CollectionUtils.isEmpty(orderList)) {
            OrderMarker.markOrders(mainPlot, subPlot, orderList);
        }
        CombinedDomainXYPlot combinedPlot = MainChartUtils.setCombinedPlot(mainPlot, subPlot);
        combinedChart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        this.setupChartPanel();
    }

    private void setupChartPanel() {
        ChartPanel chartPanel = new ChartPanel(combinedChart);
        chartPanel.setPreferredSize(new Dimension(CHART_WIDTH, CHART_HEIGHT));
        chartPanel.setMouseWheelEnabled(false);  // 禁用鼠标滚轮
        chartPanel.setMouseZoomable(false);      // 禁用鼠标缩放
        // 不设置内容面板为图表，可避免窗口跳转
        this.setVisible(false); // 隐藏窗口
        this.setContentPane(chartPanel); // 保留渲染内容
    }

    public JFreeChart getCombinedChart() {
        return combinedChart;
    }
}
