package com.gantenx.utils.chart;

import com.gantenx.constant.Series;
import com.gantenx.engine.Order;
import com.gantenx.model.Pair;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.*;
import static com.gantenx.utils.DateUtils.SIMPLE_DATE_FORMAT;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT;

@Slf4j
public class MainChartUtils {
    public static XYPlot createMainPlot(Map<Series, Map<Long, Double>> priceDataMap) {
        if (priceDataMap.keySet().size() > 4) {
            throw new IllegalArgumentException("Main plot data map beyond limit");
        }

        JFreeChart chart = ChartFactory.createXYLineChart(TITLE, TIME, PRICE, null);
        XYPlot plot = chart.getXYPlot();
        MainChartUtils.setupAxes(plot, priceDataMap);
        MainChartUtils.setupDatasetsAndRenderers(plot, MainChartUtils.createDatasets(priceDataMap));

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
        if (Objects.nonNull(subPlot)) {
            combinedPlot.add(subPlot, 1);
        }
        return combinedPlot;
    }

    public static JFreeChart getLineChart(List<Order> orders,
                                          @Nullable Pair<Series, Map<Long, Double>> subData,
                                          Map<Series, Map<Long, Double>> dataMap) {
        XYPlot mainPlot = MainChartUtils.createMainPlot(dataMap);
        XYPlot subPlot = MainChartUtils.subPlot(subData);
        return new Chart(mainPlot, subPlot, orders).getCombinedChart();
    }

    @Nullable
    public static XYPlot subPlot(@Nullable Pair<Series, Map<Long, Double>> subData) {
        if (Objects.isNull(subData)) {
            return null;
        }
        return subDefaultPlot(subData);
    }

    public static XYPlot subDefaultPlot(@Nullable Pair<Series, Map<Long, Double>> subData) {
        if (Objects.isNull(subData)) {
            return null;
        }
        Map<Long, Double> subDataMap = subData.getValue();
        Series series = subData.getKey();
        XYSeriesCollection dataset = MainChartUtils.subDataset(series, subDataMap);
        NumberAxis axis = new NumberAxis(series.name());

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        XYPlot subPlot = new XYPlot(dataset, null, axis, renderer);
        double maxValue = CollectionUtils.getMaxValue(subDataMap);
        double minValue = CollectionUtils.getMinValue(subDataMap);
        axis.setRange(minValue, maxValue);
        renderer.setSeriesPaint(0, Color.ORANGE);
        renderer.setSeriesStroke(0, BASE_STROKE);

        return subPlot;
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
