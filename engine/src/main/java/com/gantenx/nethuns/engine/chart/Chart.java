package com.gantenx.nethuns.engine.chart;

import com.gantenx.nethuns.commons.utils.CollectionUtils;
import com.gantenx.nethuns.engine.model.Order;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import static com.gantenx.nethuns.commons.constant.Constants.*;


public class Chart extends ApplicationFrame {
    private final JFreeChart combinedChart;

    private Chart(XYPlot mainPlot, XYPlot subPlot, List<Order> orderList) {
        super("Trading Line");
        if (!CollectionUtils.isEmpty(orderList)) {
            OrderMarker.markOrders(mainPlot, subPlot, orderList);
        }
        CombinedDomainXYPlot combinedPlot = Chart.setCombinedPlot(mainPlot, subPlot);
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

    public static JFreeChart get(XYPlot mainPlot, XYPlot subPlot, List<Order> orderList) {

        // 设置时间轴（DomainAxis）
        DateAxis domainAxis = new DateAxis("Time");
        domainAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
        domainAxis.setLowerMargin(0.02); // 设置左右边距
        domainAxis.setUpperMargin(0.02);
        domainAxis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);

        mainPlot.setDomainAxis(domainAxis);
        Chart chart = new Chart(mainPlot, subPlot, orderList);
        return chart.getCombinedChart();
    }

    private static CombinedDomainXYPlot setCombinedPlot(XYPlot mainPlot, XYPlot subPlot) {
        DateAxis timeAxis = (DateAxis) mainPlot.getDomainAxis();
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(timeAxis);
        combinedPlot.add(mainPlot, 3);
        if (Objects.nonNull(subPlot)) {
            combinedPlot.add(subPlot, 1);
        }
        return combinedPlot;
    }
}
