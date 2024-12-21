package com.gantenx.strategy;

import com.gantenx.model.Profit;
import com.gantenx.constant.Symbol;
import com.gantenx.engine.OrderCalculator;
import com.gantenx.engine.TradeDetail;
import com.gantenx.engine.TradeEngine;
import com.gantenx.model.Kline;
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
    protected final List<Long> openDayList;
    protected final TradeEngine tradeEngine;
    protected TradeDetail tradeDetail;

    public BaseStrategy(String name, List<Symbol> symbols, List<Long> openDayList) {
        this.strategyName = name;
        this.klineMap = KlineService.genKlineMap(symbols, openDayList);
        this.openDayList = openDayList;
        this.tradeEngine = new TradeEngine(openDayList, this.klineMap);
    }

    protected void process() {
        this.open();
        this.tradeDetail = tradeEngine.exit();
    }

    protected abstract void open();

    public void export() {
        // 构建 excel 表格
        Workbook workbook = ExcelUtils.singleSheet(Collections.singletonList(this.tradeDetail), TRADE_DETAIL);
        ExcelUtils.addDataToNewSheet(workbook, this.tradeDetail.getOrders(), ORDER_LIST);
        ExcelUtils.addDataToNewSheet(workbook, this.tradeDetail.getRecords(), RECORD_LIST);
        List<Profit> profitList = OrderCalculator.calculateProfitAndHoldingDays(this.tradeDetail.getOrders());
        ExcelUtils.addDataToNewSheet(workbook, profitList, PROFIT_LIST);

        // 导出 excel 表格
        String startStr = DateUtils.getDate(openDayList.get(0));
        String endStr = DateUtils.getDate(openDayList.get(openDayList.size() - 1));
        ExportUtils.exportWorkbook(workbook, startStr, endStr, strategyName, RESULT);

        // 保存
        JFreeChart tradingChart = this.getTradingChart();
        if (Objects.isNull(tradingChart)) {
            return;
        }
        ExportUtils.saveJFreeChartAsImage(tradingChart, startStr, endStr, strategyName, LINES, 2400, 1200);
    }

    protected JFreeChart getTradingChart() {
        return null;
    }

    public static <T extends BaseStrategy> void processAndExport(T strategy) {
        strategy.process();
        strategy.export();
    }
}
