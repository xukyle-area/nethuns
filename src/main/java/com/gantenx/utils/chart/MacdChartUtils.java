package com.gantenx.utils.chart;

import com.gantenx.constant.Series;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.model.Pair;
import com.gantenx.utils.calculator.MacdDetail;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Series.HISTOGRAM;
import static com.gantenx.constant.Series.MACD_DETAIL;

@Slf4j
public class MacdChartUtils {
    public static JFreeChart getSubMacdChart(List<Order> orders,
                                             @Nullable Map<Long, MacdDetail> subData,
                                             Pair<Series, Map<Long, Kline>> dataMap) {
        XYPlot mainPlot = CandleChartUtils.createCandlePlot(dataMap);
        XYPlot subPlot = MacdChartUtils.macdDetailPlot(subData);
        return new Chart(mainPlot, subPlot, orders).getCombinedChart();
    }

    public static XYBarRenderer createHistogramRenderer(Map<Long, MacdDetail> subDataMap,
                                                        XYSeries histogramSeries) {
        return new XYBarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                Long timestamp = (Long) histogramSeries.getX(column); // 获取 X 轴的时间戳
                MacdDetail macdDetail = subDataMap.get(timestamp);
                return macdDetail.getHistogramColor();
            }
        };
    }

    @Nullable
    public static XYPlot macdDetailPlot(@Nullable Map<Long, MacdDetail> subDataMap) {
        if (Objects.isNull(subDataMap)) {
            return null;
        }

        // 创建数据集
        XYSeries difSeries = new XYSeries("DIF");
        XYSeries emaSeries = new XYSeries("EMA");
        XYSeries histogramSeries = new XYSeries("Histogram");

        // 填充数据集
        for (Map.Entry<Long, MacdDetail> entry : subDataMap.entrySet()) {
            Long timestamp = entry.getKey();
            MacdDetail macdDetail = entry.getValue();

            if (macdDetail != null) {
                difSeries.add(timestamp, macdDetail.getMacdLine());
                emaSeries.add(timestamp, macdDetail.getSignalLine());
                histogramSeries.add(timestamp, macdDetail.getHistogram());
            }
        }

        // 直方图数据集
        XYSeriesCollection histogramDataset = new XYSeriesCollection();
        histogramDataset.addSeries(histogramSeries);

        // DIF 和 EMA 数据集
        XYSeriesCollection lineDataset = new XYSeriesCollection();
        lineDataset.addSeries(difSeries);
        lineDataset.addSeries(emaSeries);

        // 创建 XYPlot
        XYPlot subPlot = new XYPlot();

        // 直方图配置
        int histogramDatasetIndex = 0;
        int histogramAxisIndex = 0;
        NumberAxis histogramAxis = new NumberAxis(HISTOGRAM.name());
        histogramAxis.setAutoRangeIncludesZero(true);
        subPlot.setRangeAxis(histogramAxisIndex, histogramAxis);
        subPlot.setDataset(histogramDatasetIndex, histogramDataset);

        XYBarRenderer histogramRenderer = createHistogramRenderer(subDataMap, histogramSeries);
        histogramRenderer.setBarPainter(new StandardXYBarPainter());
        histogramRenderer.setShadowVisible(false);
        subPlot.setRenderer(histogramDatasetIndex, histogramRenderer);

        // DIF 和 EMA 折线图配置
        int lineDatasetIndex = 1;
        int lineAxisIndex = 1;
        NumberAxis lineAxis = new NumberAxis(MACD_DETAIL.name());
        lineAxis.setAutoRangeIncludesZero(false);
        subPlot.setRangeAxis(lineAxisIndex, lineAxis);
        subPlot.setDataset(lineDatasetIndex, lineDataset);

        XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, Color.BLUE); // DIF
        lineRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        lineRenderer.setSeriesPaint(1, Color.ORANGE); // EMA
        lineRenderer.setSeriesStroke(1, new BasicStroke(2.0f));
        lineRenderer.setDefaultShapesVisible(false);
        subPlot.setRenderer(lineDatasetIndex, lineRenderer);

        // 映射数据集到坐标轴
        subPlot.mapDatasetToRangeAxis(histogramDatasetIndex, histogramAxisIndex);
        subPlot.mapDatasetToRangeAxis(lineDatasetIndex, lineAxisIndex);

        // 添加 0 线标记
        ValueMarker zeroMarker = new ValueMarker(0);
        zeroMarker.setPaint(Color.BLACK);
        zeroMarker.setStroke(new BasicStroke(1.0f));
        subPlot.addRangeMarker(zeroMarker, Layer.FOREGROUND);

        return subPlot;
    }
}