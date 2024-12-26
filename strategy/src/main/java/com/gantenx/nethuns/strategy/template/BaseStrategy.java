package com.gantenx.nethuns.strategy.template;

import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.commons.utils.ExcelUtils;
import com.gantenx.nethuns.engine.TradeEngine;
import com.gantenx.nethuns.engine.calculator.DefaultProfitCalculator;
import com.gantenx.nethuns.engine.chart.ExportUtils;
import com.gantenx.nethuns.engine.model.Position;
import com.gantenx.nethuns.engine.model.ProfitRate;
import com.gantenx.nethuns.engine.model.TradeDetail;
import com.gantenx.nethuns.service.KlineService;
import com.gantenx.nethuns.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;

import java.util.*;

import static com.gantenx.nethuns.commons.constant.Constants.*;

@Slf4j
public abstract class BaseStrategy {
    protected final Map<Symbol, Map<Long, Kline>> klineMap;
    protected final List<Long> timestampList;
    protected final TradeEngine tradeEngine;
    protected TradeDetail tradeDetail;

    public BaseStrategy(Period period, long start, long end, List<Symbol> symbols) {
        timestampList = TimeUtils.genTimeList(period, start, end);
        this.klineMap = KlineService.getSymbolKlineMap(symbols, period, timestampList);
        this.tradeEngine = new TradeEngine(timestampList, this.klineMap);
    }

    protected void process() {
        this.open();
        this.tradeDetail = tradeEngine.exit();
    }

    protected abstract void open();

    public void export() {
        UUID uuid = UUID.randomUUID();
        String string = uuid.toString().substring(0, 16);
        // 构建 excel 表格
        Workbook workbook = ExcelUtils.singleSheet(Collections.singletonList(this.tradeDetail), TRADE_DETAIL);
        ExcelUtils.addDataToNewSheet(workbook, this.tradeDetail.getOrders(), ORDER_LIST);
        ExcelUtils.addDataToNewSheet(workbook, this.tradeDetail.getRecords(), RECORD_LIST);
        List<ProfitRate> longHoldingProfitList = DefaultProfitCalculator.calculator(timestampList, klineMap);
        ExcelUtils.addDataToNewSheet(workbook, longHoldingProfitList, LONG_HOLDING_PROFIT_RATE);
        // 导出 excel 表格
        String startStr = DateUtils.getDate(timestampList.get(0));
        String endStr = DateUtils.getDate(timestampList.get(timestampList.size() - 1));
        ExcelUtils.exportWorkbook(workbook, startStr, endStr, string, RESULT);

        // 保存
        JFreeChart tradingChart = this.getChart();
        if (Objects.nonNull(tradingChart)) {
            ExportUtils.saveJFreeChartAsImage(tradingChart, startStr, endStr, string, LINES);
        }
    }

    protected abstract JFreeChart getChart();

    protected void stopLoss(double lossRate) {
        for (Map.Entry<Symbol, Map<Long, Kline>> entry : this.klineMap.entrySet()) {
            Symbol symbol = entry.getKey();
            List<Position> positionList = tradeEngine.getPositions(symbol);
            double price = tradeEngine.getPrice(symbol);
            for (Position position : positionList) {
                double prevPrice = position.getPrice();
                double v = 1 - price / prevPrice;
                if (v > lossRate) {
                    tradeEngine.sell(symbol, position.getQuantity(), "Loss rate: " + v + ", stop");
                }
            }
        }
    }

    public static <T extends BaseStrategy> void processAndExport(T strategy) {
        strategy.process();
        strategy.export();
    }
}
