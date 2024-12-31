package com.gantenx.nethuns.rule;

import com.gantenx.nethuns.calculator.ConstantIndicator;
import com.gantenx.nethuns.calculator.CrossIndicator;
import com.gantenx.nethuns.calculator.base.Indicator;
import com.gantenx.nethuns.rule.base.AbstractRule;

import java.util.Objects;

/**
 * the value of the second one.
 */
public class CrossedUpIndicatorRule extends AbstractRule {

    /**
     * The cross indicator
     */
    private final CrossIndicator cross;

    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param threshold the threshold
     */
    public CrossedUpIndicatorRule(Indicator<Double> indicator, Double threshold) {
        this(indicator, new ConstantIndicator<>(indicator.getKlineMap(), threshold));
    }

    /**
     * Constructor.
     *
     * @param first  the first indicator
     * @param second the second indicator
     */
    public CrossedUpIndicatorRule(Indicator<Double> first, Indicator<Double> second) {
        this.cross = new CrossIndicator(second, first);
    }

    /**
     * This rule does not use the {@code tradingRecord}.
     */
    @Override
    public boolean isSatisfied(long timestamp) {
        Boolean satisfied = cross.getValue(timestamp);
        if (Objects.isNull(satisfied)) {
            return false;
        }
        super.traceIsSatisfied(timestamp, satisfied);
        return satisfied;
    }

    /**
     * @return the initial lower indicator
     */
    public Indicator<Double> getLow() {
        return cross.getLow();
    }

    /**
     * @return the initial upper indicator
     */
    public Indicator<Double> getUp() {
        return cross.getUp();
    }
}
