package com.gantenx.chart;

import com.gantenx.constant.Series;
import com.gantenx.engine.Order;
import com.gantenx.model.Pair;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
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

import java.awt.*;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.*;
import static com.gantenx.utils.DateUtils.SIMPLE_DATE_FORMAT;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT;

@Slf4j
public class ChartUtils {

    public static XYPlot createMainPlot(Map<Series, Map<Long, Double>> priceDataMap) {
        if (priceDataMap.keySet().size() > 4) {
            throw new IllegalArgumentException("Main plot data map beyond limit");
        }

        JFreeChart chart = ChartFactory.createXYLineChart(TITLE, TIME, PRICE, null);
        XYPlot plot = chart.getXYPlot();
        ChartUtils.setupAxes(plot, priceDataMap);
        ChartUtils.setupDatasetsAndRenderers(plot, ChartUtils.createDatasets(priceDataMap));

        return plot;
    }

    private static void setupAxes(XYPlot plot, Map<Series, Map<Long, Double>> priceDataMap) {
        DateAxis timeAxis = new DateAxis(TIME);
        timeAxis.setDateFormatOverride(SIMPLE_DATE_FORMAT);
        plot.setDomainAxis(timeAxis);
        int index = 0;
        for (Map.Entry<Series, Map<Long, Double>> entry : priceDataMap.entrySet()) {
            Map<Long, Double> map = entry.getValue();
            double min = CollectionUtils.getMinValue(map);
            double max = CollectionUtils.getMaxValue(map);
            NumberAxis axis = new NumberAxis(entry.getKey().name());
            double padding = (max - min) * 0.2;
            axis.setRange(Math.max(0, min - padding), max + padding);
            axis.setAutoRange(false);
            plot.setRangeAxis(index, axis);
            plot.setRangeAxisLocation(index, index % 2 == 0 ? BOTTOM_OR_LEFT : BOTTOM_OR_RIGHT);
            index++;
        }
    }

    private static XYSeriesCollection[] createDatasets(Map<Series, Map<Long, Double>> priceDataMap) {
        XYSeriesCollection[] datasets = new XYSeriesCollection[priceDataMap.size()];
        int index = 0;
        for (Map.Entry<Series, Map<Long, Double>> entry : priceDataMap.entrySet()) {
            String seriesName = entry.getKey().name();
            Map<Long, Double> dataMap = entry.getValue();
            datasets[index] = createDataset(seriesName, dataMap);
            index++;
        }
        return datasets;
    }

    private static XYSeriesCollection createDataset(String name, Map<Long, Double> dataMap) {
        XYSeries series = new XYSeries(name);
        dataMap.forEach((timestamp, kline) -> series.add(timestamp.doubleValue(), kline));
        return new XYSeriesCollection(series);
    }

    private static void setupDatasetsAndRenderers(XYPlot plot, XYSeriesCollection[] datasets) {
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.CYAN};

        for (int i = 0; i < datasets.length; i++) {
            plot.setDataset(i, datasets[i]);
            plot.mapDatasetToRangeAxis(i, i);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
            renderer.setSeriesPaint(0, colors[i]);
            renderer.setSeriesStroke(0, BASE_STROKE);
            plot.setRenderer(i, renderer);
        }
    }

    public static CombinedDomainXYPlot setCombinedPlot(XYPlot mainPlot, XYPlot subPlot) {
        DateAxis timeAxis = (DateAxis) mainPlot.getDomainAxis();
        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(timeAxis);
        combinedPlot.add(mainPlot, 3);
        combinedPlot.add(subPlot, 1);
        return combinedPlot;
    }

    /**
     * @param orders  展示在图标上的订单记录，可以为空
     * @param dataMap 主图上的数据
     * @param subData 展示在下方的图的数据
     */
    private static Chart getChart(List<Order> orders,
                                 Pair<Series, Map<Long, Double>> subData,
                                 Map<Series, Map<Long, Double>> dataMap) {
        Map<Long, Double> subDataMap = subData.getValue();
        Series series = subData.getKey();
        XYSeriesCollection dataset = ChartUtils.subDataset(series, subDataMap);
        NumberAxis axis = new NumberAxis(series.name());
        double maxValue = CollectionUtils.getMaxValue(subDataMap);
        double minValue = CollectionUtils.getMinValue(subDataMap);
        axis.setRange(minValue, maxValue);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.ORANGE);
        renderer.setSeriesStroke(0, BASE_STROKE);

        XYPlot subPlot = new XYPlot(dataset, null, axis, renderer);
        return new Chart(dataMap, subPlot, orders);
    }

    public static JFreeChart getJFreeChart(List<Order> orders,
                                           Pair<Series, Map<Long, Double>> subData,
                                           Map<Series, Map<Long, Double>> dataMap) {
        Chart chart = ChartUtils.getChart(orders, subData, dataMap);
        return chart.getCombinedChart();
    }

    private static XYSeriesCollection subDataset(Series series, Map<Long, Double> subDataMap) {
        XYSeries xySeries = new XYSeries(series.name());
        for (Map.Entry<Long, Double> entry : subDataMap.entrySet()) {
            xySeries.add(entry.getKey(), entry.getValue());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(xySeries);
        return dataset;
    }
}
