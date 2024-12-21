package com.gantenx.chart.qqq;

import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.utils.ChartUtils;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.OrderMarker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.*;

public abstract class BaseQQQChart<T> extends ApplicationFrame {
    private final JFreeChart combinedChart;
    protected BaseQQQChart(Map<Long, Kline> qqqMap,
                           Map<Long, Kline> tqqqMap,
                           Map<Long, Kline> sqqqMap,
                           XYSeriesCollection subDataset,
                           String subDataName,
                           double subDataRange,
                           List<Order<T>> orderList) {
        super("Trading Line");
        XYPlot mainPlot = createMainPlot(CollectionUtils.toPriceMap(qqqMap),
                                         CollectionUtils.toPriceMap(tqqqMap),
                                         CollectionUtils.toPriceMap(sqqqMap));
        XYPlot subPlot = ChartUtils.createSubPlot(subDataset, subDataName, subDataRange);
        OrderMarker.markOrders(mainPlot, subPlot, orderList);
        DateAxis timeAxis = (DateAxis) mainPlot.getDomainAxis();
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(timeAxis);
        combinedPlot.add(mainPlot, 3);
        combinedPlot.add(subPlot, 1);
        combinedChart = new JFreeChart("Trading Chart", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        this.setupChartPanel();
    }

    private XYPlot createMainPlot(Map<Long, Double> qqqMap,
                                  Map<Long, Double> tqqqMap,
                                  Map<Long, Double> sqqqMap) {
        XYSeriesCollection qqq = ChartUtils.createDataset("QQQ", qqqMap);
        XYSeriesCollection tqqq = ChartUtils.createDataset("TQQQ", tqqqMap);
        XYSeriesCollection sqqq = ChartUtils.createDataset("SQQQ", sqqqMap);
        XYSeriesCollection[] datasets = {qqq, tqqq, sqqq};
        JFreeChart chart = ChartFactory.createXYLineChart(
                K_LINE, TIME, PRICE, null,
                PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = chart.getXYPlot();
        plot.setDomainAxis(ChartUtils.getDateAxis());
        ChartUtils.setupPriceAxes(plot,
                                  PRICE,
                                  PRICE,
                                  CollectionUtils.getMinValue(qqqMap),
                                  CollectionUtils.getMinValue(tqqqMap),
                                  CollectionUtils.getMaxValue(qqqMap),
                                  CollectionUtils.getMaxValue(tqqqMap));
        ChartUtils.setupDatasetsAndRenderers(plot, datasets);
        return plot;
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
