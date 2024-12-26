package com.gantenx.nethuns.engine.chart.plot;


import com.gantenx.nethuns.commons.constant.Series;
import com.gantenx.nethuns.commons.utils.CollectionUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Map;

import static com.gantenx.nethuns.commons.constant.Constants.*;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_RIGHT;

public class LinePlot {

    public static XYPlot create(Map<Series, Map<Long, Double>> dataMap) {
        if (dataMap.keySet().size() > 4) {
            throw new IllegalArgumentException("Main plot data map beyond limit");
        }

        JFreeChart chart = ChartFactory.createXYLineChart(TITLE, TIME, DATA, null);
        XYPlot plot = chart.getXYPlot();
        LinePlot.setupAxes(plot, dataMap);
        LinePlot.setupDatasetsAndRenderers(plot, LinePlot.createDatasets(dataMap));

        return plot;
    }

    private static void setupAxes(XYPlot plot, Map<Series, Map<Long, Double>> seriesDataMap) {
        int index = 0;
        for (Map.Entry<Series, Map<Long, Double>> entry : seriesDataMap.entrySet()) {
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

    public static XYSeriesCollection[] createDatasets(Map<Series, Map<Long, Double>> seriesDataMap) {
        XYSeriesCollection[] datasets = new XYSeriesCollection[seriesDataMap.size()];
        int index = 0;
        for (Map.Entry<Series, Map<Long, Double>> entry : seriesDataMap.entrySet()) {
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
}
