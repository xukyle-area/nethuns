package com.gantenx.strategy.template;

import com.gantenx.constant.Period;
import com.gantenx.constant.Symbol;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;

@Slf4j
public abstract class MultiStrategy extends BaseStrategy {

    public MultiStrategy(Period period, long start, long end, List<Symbol> symbolList) {
        super(period, DateUtils.genTimeList(period, start, end), symbolList);
    }

    @Override
    public void open() {
    }


    @Override
    protected JFreeChart getChart() {
        return null;
    }
}