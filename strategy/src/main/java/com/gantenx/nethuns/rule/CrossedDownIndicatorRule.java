package com.gantenx.nethuns.rule;

import java.util.Objects;
import com.gantenx.nethuns.engine.indicator.ConstantIndicator;
import com.gantenx.nethuns.engine.indicator.CrossIndicator;
import com.gantenx.nethuns.engine.indicator.base.Indicator;
import com.gantenx.nethuns.engine.rule.AbstractRule;

/**
 * Satisfied when the value of the first {@link Indicator indicator}
 * crosses-down the value of the second one.
 */
public class CrossedDownIndicatorRule extends AbstractRule {

    /**
     * The cross indicator
     */
    private final CrossIndicator cross;

    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param threshold a threshold
     */
    public CrossedDownIndicatorRule(Indicator<Double> indicator, Double threshold) {
        this(indicator, new ConstantIndicator<>(indicator.getKlineMap(), threshold));
    }

    /**
     * Constructor.
     *
     * @param first  the first indicator
     * @param second the second indicator
     */
    public CrossedDownIndicatorRule(Indicator<Double> first, Indicator<Double> second) {
        this.cross = new CrossIndicator(first, second);
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
}
