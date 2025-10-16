package com.gantenx.nethuns.engine.chart.plot;


import static com.gantenx.nethuns.commons.constant.Index.HISTOGRAM;
import static com.gantenx.nethuns.commons.constant.Series.MACD_DETAIL;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.gantenx.nethuns.indicator.model.MacdDetail;

public class MacdPlot {

    public static XYBarRenderer createHistogramRenderer(Map<Long, MacdDetail> subDataMap, XYSeries histogramSeries) {
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
    public static XYPlot create(@Nullable Map<Long, MacdDetail> subDataMap) {
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
