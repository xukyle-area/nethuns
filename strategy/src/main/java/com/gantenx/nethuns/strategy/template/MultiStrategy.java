package com.gantenx.nethuns.strategy.template;

import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;

@Slf4j
public abstract class MultiStrategy extends BaseStrategy {

    public MultiStrategy(Period period, long start, long end, List<Symbol> symbolList) {
        super(period, start, end, symbolList);
    }

    @Override
    public void open() {
    }


    @Override
    protected JFreeChart getChart() {
        return null;
    }
}