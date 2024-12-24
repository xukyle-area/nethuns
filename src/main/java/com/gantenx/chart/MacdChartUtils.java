package com.gantenx.chart;

import com.gantenx.calculator.MacdDetail;
import com.gantenx.constant.Series;
import com.gantenx.engine.Order;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MacdChartUtils {
    public static JFreeChart getSubMacdChart(List<Order> orders,
                                             @Nullable Map<Long, MacdDetail> subData,
                                             Map<Series, Map<Long, Double>> dataMap) {
        XYPlot mainPlot = MainChartUtils.createMainPlot(dataMap);
        XYPlot subPlot = MacdChartUtils.macdDetailPlot(subData);
        return new Chart(mainPlot, subPlot, orders).getCombinedChart();
    }


    public static XYBarRenderer createHistogramRenderer(XYSeriesCollection histogramDataset,
                                                        Map<Long, MacdDetail> subDataMap,
                                                        XYSeries histogramSeries) {
        return new XYBarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                // 获取对应的时间戳（X轴的时间值）
                Long timestamp = (Long) histogramSeries.getX(column);  // 获取X轴的时间戳
                MacdDetail macdDetail = subDataMap.get(timestamp);  // 获取对应的MacdDetail

                if (macdDetail != null) {
                    // 判断是否是上升或下降并设置颜色
                    double value = histogramDataset.getYValue(row, column);
                    if (value > 0) {
                        // 直方图高度为正，且上升时是绿色
                        if (macdDetail.histogram > 0 && value > macdDetail.histogram) {
                            return Color.GREEN; // 增长并且值为正
                        } else if (macdDetail.histogram <= 0) {
                            return Color.LIGHT_GRAY; // 下降时使用 maroon
                        } else {
                            return Color.GRAY; // 否则使用灰色
                        }
                    } else {
                        // 直方图高度为负，且下降时是红色
                        if (macdDetail.histogram < 0 && value < macdDetail.histogram) {
                            return Color.RED; // 减小并且值为负
                        } else if (macdDetail.histogram >= 0) {
                            return Color.BLUE; // 负值，但上升
                        } else {
                            return Color.GRAY; // 否则使用灰色
                        }
                    }
                }
                return Color.GRAY; // 默认灰色
            }
        };
    }

    @Nullable
    public static XYPlot macdDetailPlot(@Nullable Map<Long, MacdDetail> subDataMap) {
        if (Objects.isNull(subDataMap)) {
            return null;
        }

        // 创建 MACD 数据集（包括主线和信号线）
        XYSeriesCollection macdDataset = new XYSeriesCollection();
        XYSeries macdLineSeries = new XYSeries("MACD Line");
        XYSeries signalLineSeries = new XYSeries("Signal Line");

        // 创建 Histogram 数据集
        XYSeriesCollection histogramDataset = new XYSeriesCollection();
        XYSeries histogramSeries = new XYSeries("Histogram");

        // 遍历数据，分别添加到各自的 Series
        for (Map.Entry<Long, MacdDetail> entry : subDataMap.entrySet()) {
            Long timestamp = entry.getKey();
            MacdDetail macdDetail = entry.getValue();

            // MACD 主线与信号线
            macdLineSeries.add(timestamp, macdDetail.macdLine);
            signalLineSeries.add(timestamp, macdDetail.signalLine);

            // 直方图
            histogramSeries.add(timestamp, macdDetail.histogram);
        }

        // 将 Series 添加到 Dataset
        macdDataset.addSeries(macdLineSeries);
        macdDataset.addSeries(signalLineSeries);
        histogramDataset.addSeries(histogramSeries);

        // 设置 Y 轴
        NumberAxis axis = new NumberAxis("MACD Value");
        axis.setAutoRangeIncludesZero(false);

        // 创建 XYPlot，设置主数据集（包含主线和信号线）
        XYPlot subPlot = new XYPlot(macdDataset, null, axis, null);

        // 创建并设置 Histogram 渲染器
        XYBarRenderer histogramRenderer = createHistogramRenderer(histogramDataset, subDataMap, histogramSeries);

        // 设置柱状图样式并移除阴影
        histogramRenderer.setBarPainter(new StandardXYBarPainter());
        histogramRenderer.setShadowVisible(false);

        // 为直方图设置渲染器，并将其添加到 XYPlot
        subPlot.setDataset(1, histogramDataset);
        subPlot.setRangeAxis(1, new NumberAxis("Histogram"));
        subPlot.setRenderer(1, histogramRenderer);

        // 设置数据范围
        double maxValue = getMaxValue(subDataMap);
        double minValue = getMinValue(subDataMap);
        axis.setRange(minValue, maxValue);

        // 添加 0 坐标轴
        ValueMarker zeroMarker = new ValueMarker(0); // y=0 的线
        zeroMarker.setPaint(Color.BLACK);           // 线条颜色
        zeroMarker.setStroke(new BasicStroke(1.0f)); // 线条粗细
        subPlot.addRangeMarker(zeroMarker, Layer.FOREGROUND); // 添加到前景层

        return subPlot;
    }

    // 获取最小值
    public static double getMinValue(Map<Long, MacdDetail> subDataMap) {
        double minValue = Double.POSITIVE_INFINITY;
        for (MacdDetail macdDetail : subDataMap.values()) {
            if (macdDetail != null) {
                minValue = Math.min(minValue,
                                    Math.min(macdDetail.macdLine,
                                             Math.min(macdDetail.signalLine, macdDetail.histogram)));
            }
        }
        return minValue;
    }

    // 获取最大值
    public static double getMaxValue(Map<Long, MacdDetail> subDataMap) {
        double maxValue = Double.NEGATIVE_INFINITY;
        for (MacdDetail macdDetail : subDataMap.values()) {
            if (macdDetail != null) {
                maxValue = Math.max(maxValue,
                                    Math.max(macdDetail.macdLine,
                                             Math.max(macdDetail.signalLine, macdDetail.histogram)));
            }
        }
        return maxValue;
    }
}