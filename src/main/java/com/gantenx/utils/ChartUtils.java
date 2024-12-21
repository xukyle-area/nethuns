package com.gantenx.utils;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Map;

import static com.gantenx.utils.DateUtils.SIMPLE_DATE_FORMAT;

public class ChartUtils {

    private static final String TIME = "Time";
    private static final float LINE_STROKE_WIDTH = 2.0f;
    public static final BasicStroke BASE_STROKE = new BasicStroke(LINE_STROKE_WIDTH);

    public static XYSeriesCollection createDataset(String name, Map<Long, Double> dataMap) {
        XYSeries series = new XYSeries(name);
        dataMap.forEach((timestamp, kline) -> series.add(timestamp.doubleValue(), kline));
        return new XYSeriesCollection(series);
    }

    public static XYPlot createSubPlot(XYSeriesCollection dataset, String name, double range) {
        NumberAxis axis = new NumberAxis(name);
        axis.setRange(0.0, range);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.ORANGE);
        renderer.setSeriesStroke(0, BASE_STROKE);

        return new XYPlot(dataset, null, axis, renderer);
    }

    public static void setupDatasetsAndRenderers(XYPlot plot, XYSeriesCollection[] datasets) {
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN};

        for (int i = 0; i < datasets.length; i++) {
            plot.setDataset(i, datasets[i]);
            plot.mapDatasetToRangeAxis(i, i == 0 ? 0 : 1);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
            renderer.setSeriesPaint(0, colors[i]);
            renderer.setSeriesStroke(0, BASE_STROKE);
            plot.setRenderer(i, renderer);
        }
    }

    public static void setupPriceAxes(XYPlot plot,
                                      String leftName,
                                      String rightName,
                                      double leftMin,
                                      double rightMin,
                                      double leftMax,
                                      double rightMax) {
        NumberAxis qqqAxis = new NumberAxis(leftName);
        qqqAxis.setRange(leftMin * 0.95, leftMax * 1.05);
        plot.setRangeAxis(0, qqqAxis);

        NumberAxis leveragedAxis = new NumberAxis(rightName);
        leveragedAxis.setRange(rightMin * 0.95, rightMax * 1.05);
        plot.setRangeAxis(1, leveragedAxis);
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
    }

    public static DateAxis getDateAxis() {
        DateAxis timeAxis = new DateAxis(TIME);
        timeAxis.setDateFormatOverride(SIMPLE_DATE_FORMAT);
        return timeAxis;
    }
}
