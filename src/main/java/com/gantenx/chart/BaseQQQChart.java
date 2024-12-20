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

    // 图表尺寸
    private static final int CHART_WIDTH = 2400;  // 增加宽度
    private static final int CHART_HEIGHT = 1200;

    // 轴范围
    private static final double QQQ_AXIS_MAX = 550;
    private static final double LEVERAGED_AXIS_MAX = 250;

    // 线条样式
    private static final float LINE_STROKE_WIDTH = 2.0f;
    private static final BasicStroke BASE_STROKE = new BasicStroke(LINE_STROKE_WIDTH);

    private final JFreeChart combinedChart;

    protected BaseQQQChart(Map<Long, Kline> qqqMap,
                           Map<Long, Kline> tqqqMap,
                           Map<Long, Kline> sqqqMap,
                           XYSeriesCollection subDataset,
                           String subDataName,
                           double subDataRange,
                           List<Order> orderList) {
        super("Trading Line");

        // 创建主图表
        XYPlot mainPlot = createMainPlot(qqqMap, tqqqMap, sqqqMap);

        // 创建子图表
        XYPlot subPlot = createSubPlot(subDataset, subDataName, subDataRange);

        // 添加交易标记
        TradeAnnotationManager.markOrders(mainPlot, subPlot, orderList);

        // 创建组合图表
        DateAxis timeAxis = (DateAxis) mainPlot.getDomainAxis();
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(timeAxis);
        combinedPlot.add(mainPlot, 3);
        combinedPlot.add(subPlot, 1);

        // 创建最终图表
        combinedChart = new JFreeChart("Trading Chart", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        setupChartPanel();
    }

    private XYPlot createMainPlot(Map<Long, Kline> qqqMap,
                                  Map<Long, Kline> tqqqMap,
                                  Map<Long, Kline> sqqqMap) {
        // 创建数据集
        XYSeriesCollection[] datasets = {
                createKlineDataset("QQQ", qqqMap),
                createKlineDataset("TQQQ", tqqqMap),
                createKlineDataset("SQQQ", sqqqMap)
        };

        // 创建基础图表
        JFreeChart chart = ChartFactory.createXYLineChart(
                K_LINE, TIME, PRICE, null,
                PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = chart.getXYPlot();

        // 设置时间轴
        setupTimeAxis(plot);

        // 设置价格轴
        setupPriceAxes(plot);

        // 设置数据集和渲染器
        setupDatasetsAndRenderers(plot, datasets);

        return plot;
    }

    private void setupTimeAxis(XYPlot plot) {
        DateAxis timeAxis = new DateAxis(TIME);
        timeAxis.setDateFormatOverride(SIMPLE_DATE_FORMAT);
        plot.setDomainAxis(timeAxis);
    }

    private void setupPriceAxes(XYPlot plot) {
        // QQQ轴（左）
        NumberAxis qqqAxis = new NumberAxis("QQQ Price");
        qqqAxis.setRange(0, QQQ_AXIS_MAX);
        plot.setRangeAxis(0, qqqAxis);

        // TQQQ/SQQQ共用轴（右）
        NumberAxis leveragedAxis = new NumberAxis("TQQQ/SQQQ Price");
        leveragedAxis.setRange(0, LEVERAGED_AXIS_MAX);
        plot.setRangeAxis(1, leveragedAxis);
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
    }

    private void setupDatasetsAndRenderers(XYPlot plot, XYSeriesCollection[] datasets) {
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN};

        for (int i = 0; i < datasets.length; i++) {
            // 设置数据集
            plot.setDataset(i, datasets[i]);

            // 设置轴映射（TQQQ和SQQQ使用右轴）
            plot.mapDatasetToRangeAxis(i, i == 0 ? 0 : 1);

            // 设置渲染器
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
            renderer.setSeriesPaint(0, colors[i]);
            renderer.setSeriesStroke(0, BASE_STROKE);
            plot.setRenderer(i, renderer);
        }
    }

    private XYPlot createSubPlot(XYSeriesCollection dataset, String name, double range) {
        NumberAxis axis = new NumberAxis(name);
        axis.setRange(0.0, range);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.ORANGE);
        renderer.setSeriesStroke(0, BASE_STROKE);

        return new XYPlot(dataset, null, axis, renderer);
    }

    private void setupChartPanel() {
        ChartPanel chartPanel = new ChartPanel(combinedChart);
        chartPanel.setPreferredSize(new Dimension(CHART_WIDTH, CHART_HEIGHT));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setMouseZoomable(true);
        setContentPane(chartPanel);
    }

    private XYSeriesCollection createKlineDataset(String name, Map<Long, Kline> klineMap) {
        XYSeries series = new XYSeries(name);
        klineMap.forEach((timestamp, kline) ->
                series.add(timestamp.doubleValue(), kline.getClose()));
        return new XYSeriesCollection(series);
    }

    public JFreeChart getCombinedChart() {
        return combinedChart;
    }
}
