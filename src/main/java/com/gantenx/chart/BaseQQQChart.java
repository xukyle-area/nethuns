package com.gantenx.chart;

import com.gantenx.model.Kline;
import com.gantenx.model.Order;
import com.gantenx.utils.TradeAnnotationManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.gantenx.utils.DateUtils.SIMPLE_DATE_FORMAT;

public abstract class BaseQQQChart extends ApplicationFrame {
    private static final String TIME = "Time";
    private static final String PRICE = "Price";
    private static final String K_LINE = "K-Line";
    private final JFreeChart combinedChart;

    protected BaseQQQChart(Map<Long, Kline> qqqMap, Map<Long, Kline> tqqqMap, Map<Long, Kline> sqqqMap, XYSeriesCollection subDataset,
                           String subDataName, double subDataRange, List<Order> orderList) {
        super("Trading Line");

        // 1. 创建数据集
        XYSeriesCollection qqqDataset = createKlineDataset("QQQ", qqqMap);
        XYSeriesCollection tqqqDataset = createKlineDataset("TQQQ", tqqqMap);
        XYSeriesCollection sqqqDataset = createKlineDataset("SQQQ", sqqqMap);

        // 2. 创建主图表
        JFreeChart chart = ChartFactory.createXYLineChart(
                K_LINE, TIME, PRICE, null,
                PlotOrientation.VERTICAL, true, true, false);

        XYPlot mainPlot = chart.getXYPlot();

        // 3. 设置时间轴
        DateAxis timeAxis = new DateAxis(TIME);
        timeAxis.setDateFormatOverride(SIMPLE_DATE_FORMAT);
        mainPlot.setDomainAxis(timeAxis);

        // 4. 设置Y轴
        // QQQ轴（左）
        NumberAxis qqqAxis = new NumberAxis("QQQ Price");
        qqqAxis.setRange(0, 550); // QQQ从0开始
        mainPlot.setRangeAxis(0, qqqAxis);

        // TQQQ/SQQQ共用轴（右）
        NumberAxis leveragedAxis = new NumberAxis("TQQQ/SQQQ Price");
        leveragedAxis.setRange(0, 250); // 调整范围以避免与QQQ重叠
        mainPlot.setRangeAxis(1, leveragedAxis);
        mainPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

        // 5. 设置数据集和映射
        mainPlot.setDataset(0, qqqDataset);
        mainPlot.mapDatasetToRangeAxis(0, 0);

        mainPlot.setDataset(1, tqqqDataset);
        mainPlot.mapDatasetToRangeAxis(1, 1);

        mainPlot.setDataset(2, sqqqDataset);
        mainPlot.mapDatasetToRangeAxis(2, 1); // SQQQ也使用第二个轴

        // 6. 设置渲染器
        // QQQ渲染器
        XYLineAndShapeRenderer qqqRenderer = new XYLineAndShapeRenderer(true, false);
        qqqRenderer.setSeriesPaint(0, Color.BLUE);
        qqqRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        mainPlot.setRenderer(0, qqqRenderer);

        // TQQQ渲染器
        XYLineAndShapeRenderer tqqqRenderer = new XYLineAndShapeRenderer(true, false);
        tqqqRenderer.setSeriesPaint(0, Color.RED);
        tqqqRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        mainPlot.setRenderer(1, tqqqRenderer);

        // SQQQ渲染器
        XYLineAndShapeRenderer sqqqRenderer = new XYLineAndShapeRenderer(true, false);
        sqqqRenderer.setSeriesPaint(0, Color.GREEN);
        sqqqRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        mainPlot.setRenderer(2, sqqqRenderer);

        NumberAxis rsiAxis = new NumberAxis(subDataName);
        rsiAxis.setRange(0.0, subDataRange);
        XYPlot rsiPlot = new XYPlot(subDataset, null, rsiAxis, new XYLineAndShapeRenderer(true, false));

        XYLineAndShapeRenderer rsiRenderer = (XYLineAndShapeRenderer) rsiPlot.getRenderer();
        rsiRenderer.setSeriesPaint(0, Color.ORANGE);
        rsiRenderer.setSeriesStroke(0, new BasicStroke(2.0f));

        TradeAnnotationManager.markOrders(mainPlot, rsiPlot, orderList);

        // 创建组合图表
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(timeAxis);
        combinedPlot.add(mainPlot, 3);
        combinedPlot.add(rsiPlot, 1);

        // 创建最终图表
        combinedChart = new JFreeChart("Trading Chart", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

        // 设置图表面板
        ChartPanel chartPanel = new ChartPanel(combinedChart);
        chartPanel.setPreferredSize(new Dimension(1600, 1200));
        setContentPane(chartPanel);
    }

    private XYSeriesCollection createKlineDataset(String name, Map<Long, Kline> klineMap) {
        XYSeries series = new XYSeries(name);
        for (Map.Entry<Long, Kline> entry : klineMap.entrySet()) {
            series.add((double) entry.getKey(), entry.getValue().getClose());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    public JFreeChart getCombinedChart() {
        return combinedChart;
    }
}
