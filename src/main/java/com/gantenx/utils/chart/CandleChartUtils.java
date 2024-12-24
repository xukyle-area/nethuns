package com.gantenx.utils.chart;

import com.gantenx.constant.Series;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.model.Pair;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.*;
import static com.gantenx.utils.DateUtils.SIMPLE_DATE_FORMAT;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT;

@Slf4j
public class CandleChartUtils {

    public static JFreeChart getCandleChart(List<Order> orders,
                                            @Nullable Pair<Series, Map<Long, Double>> subData,
                                            Pair<Series, Map<Long, Kline>> pair) {

        XYPlot subPlot = MainChartUtils.subPlot(subData);
        XYPlot mainPlot = CandleChartUtils.createCandlePlot(pair);
        return new Chart(mainPlot, subPlot, orders).getCombinedChart();
    }

    private static XYPlot createCandlePlot(Pair<Series, Map<Long, Kline>> klineDataMap) {
        DefaultHighLowDataset dataset = CandleChartUtils.createKlineDataset(klineDataMap);
        JFreeChart chart = ChartFactory.createCandlestickChart(CANDLE, TIME, PRICE, dataset, Boolean.FALSE);

        // 设置 CandlestickRenderer 以渲染 K 线
        XYPlot plot = chart.getXYPlot();
        plot.setRenderer(new CandlestickRenderer());

        // 设置时间格式
        DateAxis timeAxis = new DateAxis(TIME);
        timeAxis.setDateFormatOverride(SIMPLE_DATE_FORMAT);
        plot.setDomainAxis(timeAxis);


        Pair<Double, Double> range = CollectionUtils.getRange(klineDataMap.getValue());
        NumberAxis axis = new NumberAxis(klineDataMap.getKey().name());
        Double min = range.getFirst();
        Double max = range.getSecond();
        double padding = (max - min) * 0.05;
        axis.setRange(Math.max(0, min - padding), max + padding);
        axis.setAutoRange(false);
        plot.setRangeAxis(0, axis);
        plot.setRangeAxisLocation(0, BOTTOM_OR_LEFT);

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
