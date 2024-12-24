package com.gantenx.strategy.template;

import com.gantenx.utils.calculator.LongHoldingProfitCalculator;
import com.gantenx.utils.calculator.OrderCalculator;
import com.gantenx.constant.Period;
import com.gantenx.constant.Proportion;
import com.gantenx.constant.Symbol;
import com.gantenx.engine.TradeDetail;
import com.gantenx.engine.TradeEngine;
import com.gantenx.model.Kline;
import com.gantenx.model.Profit;
import com.gantenx.model.ProfitRate;
import com.gantenx.service.KlineService;
import com.gantenx.utils.DateUtils;
import com.gantenx.utils.ExcelUtils;
import com.gantenx.utils.ExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.*;

@Slf4j
public abstract class BaseStrategy {
    protected final String strategyName;
    protected final Map<Symbol, Map<Long, Kline>> klineMap;
    protected final List<Long> timestampList;
    protected final TradeEngine tradeEngine;
    protected TradeDetail tradeDetail;

    public BaseStrategy(String name, Period period, List<Long> timestampList, List<Symbol> symbols) {
        this.strategyName = name;
        this.klineMap = KlineService.getSymbolKlineMap(symbols, period, timestampList);
        this.timestampList = timestampList;
        this.tradeEngine = new TradeEngine(timestampList, this.klineMap);
    }

    protected void process() {
        this.open();
        this.tradeDetail = tradeEngine.exit();
    }

    protected abstract void open();

    protected Proportion buyProportion(Proportion proportion) {
        double m = INITIAL_BALANCE * proportion.getValue();
        double v = m / tradeEngine.getBalance();
        return Proportion.getProportion(v);
    }

    protected Proportion sellProportion(Proportion proportion, Symbol symbol) {
        double m = INITIAL_BALANCE * proportion.getValue();
        double price = tradeEngine.getPrice(symbol);
        double quantity = tradeEngine.getQuantity(symbol);

        double v = (m / price) / quantity;
        return Proportion.getProportion(v);
    }

    public void export() {
        // 构建 excel 表格
        Workbook workbook = ExcelUtils.singleSheet(Collections.singletonList(this.tradeDetail), TRADE_DETAIL);
        ExcelUtils.addDataToNewSheet(workbook, this.tradeDetail.getOrders(), ORDER_LIST);
        ExcelUtils.addDataToNewSheet(workbook, this.tradeDetail.getRecords(), RECORD_LIST);
        List<Profit> profitList = OrderCalculator.calculateProfitAndHoldingDays(this.tradeDetail.getOrders());
        ExcelUtils.addDataToNewSheet(workbook, profitList, PROFIT_LIST);
        List<ProfitRate> longHoldingProfitList = LongHoldingProfitCalculator.calculator(timestampList, klineMap);
        ExcelUtils.addDataToNewSheet(workbook, longHoldingProfitList, LONG_HOLDING_PROFIT_RATE);
        // 导出 excel 表格
        String startStr = DateUtils.getDate(timestampList.get(0));
        String endStr = DateUtils.getDate(timestampList.get(timestampList.size() - 1));
        ExportUtils.exportWorkbook(workbook, startStr, endStr, strategyName, RESULT);

        // 保存
        JFreeChart tradingChart = this.getChart();
        if (Objects.nonNull(tradingChart)) {
            ExportUtils.saveJFreeChartAsImage(tradingChart, startStr, endStr, strategyName, LINES);
        }
    }

    protected abstract JFreeChart getChart();


    public static <T extends BaseStrategy> void processAndExport(T strategy) {
        strategy.process();
        strategy.export();
    }
}
