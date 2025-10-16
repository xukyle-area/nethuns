package com.gantenx.nethuns.engine.chart.plot;

import static com.gantenx.nethuns.commons.constant.Constants.CANDLE;
import static com.gantenx.nethuns.commons.constant.Constants.PRICE;
import static com.gantenx.nethuns.commons.constant.Constants.TIME;
import static org.jfree.chart.axis.AxisLocation.BOTTOM_OR_LEFT;
import java.util.Date;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import com.gantenx.nethuns.commons.constant.Series;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.model.Pair;

public class CandlePlot {

    /**
     * 创建 k 线图表
     *
     * @param series       图表名称，一般与 symbol 对应
     * @param klineDataMap k 线数据
     * @return k 线图表
     */
    public static XYPlot create(Series series, Map<Long, Kline> klineDataMap) {
        DefaultHighLowDataset dataset = CandlePlot.createKlineDataset(series, klineDataMap);
        JFreeChart chart = ChartFactory.createCandlestickChart(CANDLE, TIME, PRICE, dataset, Boolean.FALSE);

        // 设置 CandlestickRenderer 以渲染 K 线
        XYPlot plot = chart.getXYPlot();
        plot.setRenderer(new CandlestickRenderer());

        Pair<Double, Double> range = getRange(klineDataMap);
        NumberAxis axis = new NumberAxis(series.name());
        Double min = range.getFirst();
        Double max = range.getSecond();
        double padding = (max - min) * 0.05;
        axis.setRange(Math.max(0, min - padding), max + padding);
        axis.setAutoRange(false);
        plot.setRangeAxis(0, axis);
        plot.setRangeAxisLocation(0, BOTTOM_OR_LEFT);

        return plot;
    }


    private static Pair<Double, Double> getRange(Map<Long, Kline> map) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (Kline kline : map.values()) {
            min = Math.min(min, kline.getLow());
            max = Math.max(max, kline.getHigh());
        }

        return Pair.create(min, max);
    }

    private static DefaultHighLowDataset createKlineDataset(Series series, Map<Long, Kline> klineData) {
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

            dates[i] = new Date(timestamp);
            high[i] = kline.getHigh();
            low[i] = kline.getLow();
            open[i] = kline.getOpen();
            close[i] = kline.getClose();
            volume[i] = kline.getVolume();
            i++;
        }

        return new DefaultHighLowDataset(series, dates, high, low, open, close, volume);
    }
}
