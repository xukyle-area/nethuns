package com.gantenx.chart;

import com.gantenx.constant.Series;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.model.Pair;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.*;
import static com.gantenx.utils.DateUtils.SIMPLE_DATE_FORMAT;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT;

@Slf4j
public class CandleChartUtils {


    private static XYPlot createMainPlot(Pair<Series, Map<Long, Kline>> klineDataMap, String title) {
        DefaultHighLowDataset dataset = CandleChartUtils.createKlineDataset(klineDataMap);
        JFreeChart chart = ChartFactory.createCandlestickChart(title, TIME, PRICE, dataset, Boolean.FALSE);

        // 设置 CandlestickRenderer 以渲染 K 线
        XYPlot plot = chart.getXYPlot();
        plot.setRenderer(new CandlestickRenderer());

        // 设置时间格式
        DateAxis timeAxis = new DateAxis(TIME);
        timeAxis.setDateFormatOverride(SIMPLE_DATE_FORMAT);
        plot.setDomainAxis(timeAxis);
        return plot;
    }

    private static DefaultHighLowDataset createKlineDataset(Pair<Series, Map<Long, Kline>> klineDataMap) {
        Map<Long, Kline> klineData = klineDataMap.getValue();
        int dataSize = klineData.size();
        Date[] dates = new Date[dataSize];
        double[] high = new double[dataSize];
        double[] low = new double[dataSize];
        double[] open = new double[dataSize];
        double[] close = new double[dataSize];
        double[] volume = new double[dataSize];

        int i = 0;
        for (Map.Entry<Long, Kline> entry : klineData.entrySet()) {
            long timestamp = entry.getKey();
            Kline kline = entry.getValue();

            dates[i] = new Date(timestamp); // 转换时间戳为 Date 类型
            high[i] = kline.getHigh();
            low[i] = kline.getLow();
            open[i] = kline.getOpen();
            close[i] = kline.getClose();
            volume[i] = kline.getVolume();
            i++;
        }

        return new DefaultHighLowDataset(klineDataMap.getKey(), dates, high, low, open, close, volume);
    }

}
