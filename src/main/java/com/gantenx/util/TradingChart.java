package com.gantenx.util;

import com.gantenx.model.Kline;
import com.gantenx.model.KlineWithRSI;
import com.gantenx.model.Order;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.util.List;

import static com.gantenx.util.DateUtils.SIMPLE_DATE_FORMAT;
public class TradingChart extends ApplicationFrame {
    private static final String TIME = "Time";
    private static final String PRICE = "Price";
    private static final String K_LINE = "K-Line";

    public TradingChart(List<KlineWithRSI> qqqList, List<Kline> tqqqList, List<Order> orderList) {
        super("Trading Line");
        XYSeriesCollection qqqKlineDataset = createKlineDataset("QQQ K-Line", qqqList);
        XYSeriesCollection tqqqKlineDataset = createKlineDataset("TQQQ K-Line", tqqqList);
        XYSeriesCollection qqqRsiDataset = createRsiDataset("QQQ RSI", qqqList);

        // 创建主图表
        JFreeChart chart = ChartFactory.createXYLineChart(K_LINE, TIME, PRICE, qqqKlineDataset, PlotOrientation.VERTICAL, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        XYPlot plot = chart.getXYPlot();

        // 设置时间轴
        DateAxis timeAxis = new DateAxis(TIME);
        timeAxis.setDateFormatOverride(SIMPLE_DATE_FORMAT);
        plot.setDomainAxis(timeAxis);

        // QQQ 使用左轴
        NumberAxis qqqPriceAxis = new NumberAxis("QQQ Price");
        plot.setRangeAxis(0, qqqPriceAxis);
        plot.setDataset(0, qqqKlineDataset);
        plot.mapDatasetToRangeAxis(0, 0);

        // TQQQ 使用右轴
        NumberAxis tqqqPriceAxis = new NumberAxis("TQQQ Price");
        tqqqPriceAxis.setRange(0, 150); // 设置范围
        plot.setRangeAxis(1, tqqqPriceAxis);
        plot.setDataset(1, tqqqKlineDataset);
        plot.mapDatasetToRangeAxis(1, 1);

        // 添加 QQQ 的 RSI 数据和对应的 RSI 轴
        NumberAxis rsiAxis = new NumberAxis("RSI");
        rsiAxis.setRange(0.0, 100.0);
        XYPlot rsiPlot = new XYPlot(qqqRsiDataset, null, rsiAxis, null);
        rsiPlot.setDataset(0, qqqRsiDataset);

        XYLineAndShapeRenderer qqqRenderer = new XYLineAndShapeRenderer();
        qqqRenderer.setSeriesPaint(0, Color.BLUE); // QQQ K-Line 颜色
        qqqRenderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(0, qqqRenderer);

        XYLineAndShapeRenderer tqqqRenderer = new XYLineAndShapeRenderer();
        tqqqRenderer.setSeriesPaint(0, Color.RED); // TQQQ K-Line 颜色
        tqqqRenderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(1, tqqqRenderer);

        XYLineAndShapeRenderer rsiRenderer = new XYLineAndShapeRenderer();
        rsiRenderer.setSeriesPaint(0, Color.GREEN); // QQQ RSI 颜色
        rsiRenderer.setSeriesShapesVisible(0, false);
        rsiPlot.setRenderer(0, rsiRenderer);


        orderList.stream().map(Order::getTimestamp).distinct().forEach(timestamp -> {
            XYLineAnnotation lineAnnotation = new XYLineAnnotation(
                    timestamp, 0, timestamp, 540,
                    new BasicStroke(1.0f), Color.BLACK);
            plot.addAnnotation(lineAnnotation);
            rsiPlot.addAnnotation(lineAnnotation);
        });

        // 创建组合图
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(timeAxis);
        combinedPlot.add(plot, 3);
        combinedPlot.add(rsiPlot, 1);

        // 设置组合图表
        JFreeChart combinedChart = new JFreeChart("Trading Chart", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        ChartPanel panel = new ChartPanel(combinedChart);
        panel.setPreferredSize(new Dimension(1600, 1200));
        setContentPane(panel);
    }

    // 创建 K 线数据集
    private <T extends Kline> XYSeriesCollection createKlineDataset(String name, List<T> klineList) {
        XYSeries series = new XYSeries(name);
        for (T k : klineList) {
            series.add(k.getTime(), Double.parseDouble(k.getClose()));
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    // 创建 RSI 数据集
    private XYSeriesCollection createRsiDataset(String name, List<KlineWithRSI> klineWithRsiList) {
        XYSeries series = new XYSeries(name);
        for (KlineWithRSI klineWithRsi : klineWithRsiList) {
            series.add(klineWithRsi.getTime(), klineWithRsi.getRsi());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    public static void show(List<KlineWithRSI> qqqList, List<Kline> tqqqList, List<Order> orderList) {
        TradingChart chart = new TradingChart(qqqList, tqqqList, orderList);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}


