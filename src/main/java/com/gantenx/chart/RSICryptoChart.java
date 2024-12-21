package com.gantenx.chart;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.constant.CryptoSymbol;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import lombok.extern.slf4j.Slf4j;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.INDEX_PERIOD;

@Slf4j
public class RSICryptoChart extends BaseCryptoChart<CryptoSymbol> {
    private static final String RSI = "RSI";

    public RSICryptoChart(Map<Long, Kline> priceMap,
                          Map<Long, Double> assetMap,
                          List<Order<CryptoSymbol>> orderList) {
        super(priceMap, assetMap, RSICryptoChart.subDataset(priceMap), RSI, 100.0, orderList);
    }

    private static XYSeriesCollection subDataset(Map<Long, Kline> klineMap) {
        Map<Long, Double> rsiMap = IndexTechnicalIndicators.calculateRSI(klineMap, INDEX_PERIOD.getRsi());
        XYSeries series = new XYSeries(RSI);

        for (Map.Entry<Long, Double> entry : rsiMap.entrySet()) {
            series.add((double) entry.getKey(), entry.getValue());
            log.info("subdata, key:{}, value:{}", entry.getKey(), entry.getValue());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }
}
