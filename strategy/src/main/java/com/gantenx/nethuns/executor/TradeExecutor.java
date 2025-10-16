package com.gantenx.nethuns.executor;

import static com.gantenx.nethuns.commons.constant.Constants.ORDER_LIST;
import static com.gantenx.nethuns.commons.constant.Constants.RECORD_LIST;
import static com.gantenx.nethuns.commons.constant.Constants.TRADE_DETAIL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import com.gantenx.nethuns.commons.constant.Proportion;
import com.gantenx.nethuns.commons.constant.Series;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.commons.utils.ExcelUtils;
import com.gantenx.nethuns.engine.TradeEngine;
import com.gantenx.nethuns.engine.chart.Chart;
import com.gantenx.nethuns.engine.chart.ExportUtils;
import com.gantenx.nethuns.engine.chart.plot.CandlePlot;
import com.gantenx.nethuns.engine.model.TradeRecord;
import com.gantenx.nethuns.rule.base.Rule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradeExecutor {
    protected final Map<Long, Candle> klineMap;
    protected final List<Long> timeList;
    protected final TradeEngine tradeEngine;
    protected final Symbol symbol;
    protected TradeRecord tradeRecord;
    private final Rule entryRule;
    private final Rule exitRule;

    public TradeExecutor(Map<Long, Candle> klineMap, Symbol symbol, Rule entryRule, Rule exitRule) {
        this.symbol = symbol;
        this.timeList = klineMap.keySet().stream().sorted().collect(Collectors.toList());
        this.tradeEngine = new TradeEngine(timeList, Collections.singletonMap(symbol, klineMap));
        this.entryRule = entryRule;
        this.exitRule = exitRule;
        this.klineMap = klineMap;
    }

    void process() {
        while (tradeEngine.hasNext()) {
            long today = tradeEngine.next();
            if (entryRule.isSatisfied(today)) {
                tradeEngine.buy(symbol, Proportion.PROPORTION_OF_100);
            } else if (exitRule.isSatisfied(today)) {
                tradeEngine.sell(symbol, Proportion.PROPORTION_OF_100);
            }
        }
        this.tradeRecord = tradeEngine.exit();
    }

    void export() {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0, 16);
        Workbook workbook = ExcelUtils.singleSheet(Collections.singletonList(this.tradeRecord), TRADE_DETAIL);
        ExcelUtils.addDataToNewSheet(workbook, this.tradeRecord.getOrders(), ORDER_LIST);
        ExcelUtils.addDataToNewSheet(workbook, this.tradeRecord.getRecords(), RECORD_LIST);

        ExcelUtils.exportWorkbook(workbook, uid);
        ExportUtils.saveJFreeChartAsImage(this.getChart(), uid);
    }

    public JFreeChart getChart() {
        XYPlot main = CandlePlot.create(Series.getSeries(symbol), klineMap);
        return Chart.get(main, null, tradeRecord.getOrders());
    }

    public static <T extends TradeExecutor> void processAndExport(T executor) {
        executor.process();
        executor.export();
    }
}
