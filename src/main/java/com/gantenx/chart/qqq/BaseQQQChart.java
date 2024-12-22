package com.gantenx.chart.qqq;

import com.gantenx.constant.Symbol;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.chart.ChartUtils;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.chart.OrderMarker;
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
import java.util.stream.Collectors;

import static com.gantenx.constant.Constants.*;
import static com.gantenx.constant.Symbol.QQQUSD;
import static com.gantenx.constant.Symbol.TQQQUSD;

public abstract class BaseQQQChart extends ApplicationFrame {
    private final JFreeChart combinedChart;

    protected BaseQQQChart(Map<Symbol, Map<Long, Kline>> klineMap,
                           XYSeriesCollection subDataset,
                           String subDataName,
                           double subDataRange,
                           List<Order> orderList) {
        super("Trading Line");
        XYPlot mainPlot = createMainPlot(klineMap);
        XYPlot subPlot = ChartUtils.createSubPlot(subDataset, subDataName, subDataRange);
        OrderMarker.markOrders(mainPlot, subPlot, orderList);
        DateAxis timeAxis = (DateAxis) mainPlot.getDomainAxis();
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(timeAxis);
        combinedPlot.add(mainPlot, 3);
        combinedPlot.add(subPlot, 1);
        combinedChart = new JFreeChart("Trading Chart", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        this.setupChartPanel();
    }

    private XYPlot createMainPlot(Map<Symbol, Map<Long, Kline>> klineMap) {
        // 1. 数据准备
        Map<Symbol, Map<Long, Double>> priceDataMap = klineMap.keySet().stream().collect(Collectors.toMap(
                symbol -> symbol,
                symbol -> CollectionUtils.toPriceMap(klineMap.get(symbol))));
        // 2. 创建数据集
        XYSeriesCollection[] datasets = klineMap.keySet().stream()
                .map(symbol -> ChartUtils.createDataset(symbol.name(), priceDataMap.get(symbol)))
                .toArray(XYSeriesCollection[]::new);
        // 3. 创建图表
        JFreeChart chart = ChartFactory.createXYLineChart(
                K_LINE, TIME, PRICE, null,
                PlotOrientation.VERTICAL, true, true, false);

        // 4. 配置绘图区域
        XYPlot plot = chart.getXYPlot();
        plot.setDomainAxis(ChartUtils.getDateAxis());
        setupAxisRanges(plot, priceDataMap);
        ChartUtils.setupDatasetsAndRenderers(plot, datasets);

        return plot;
    }

    // 抽取坐标轴设置逻辑
    private void setupAxisRanges(XYPlot plot, Map<Symbol, Map<Long, Double>> priceDataMap) {
        Map<Long, Double> qqqData = priceDataMap.get(QQQUSD);
        Map<Long, Double> tqqqData = priceDataMap.get(TQQQUSD);

        ChartUtils.setupAxes(plot,
                             PRICE,
                             PRICE,
                             CollectionUtils.getMinValue(qqqData),
                             CollectionUtils.getMinValue(tqqqData),
                             CollectionUtils.getMaxValue(qqqData),
                             CollectionUtils.getMaxValue(tqqqData));
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
