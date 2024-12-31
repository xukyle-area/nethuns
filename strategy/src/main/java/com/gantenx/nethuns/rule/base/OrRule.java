package com.gantenx.nethuns.rule.base;

public class OrRule extends AbstractRule {

    private final Rule rule1;
    private final Rule rule2;

    /**
     * Constructor.
     *
     * @param rule1 a trading rule
     * @param rule2 another trading rule
     */
    public OrRule(Rule rule1, Rule rule2) {
        this.rule1 = rule1;
        this.rule2 = rule2;
    }

    @Override
    public boolean isSatisfied(long timestamp) {
        final boolean satisfied = rule1.isSatisfied(timestamp) || rule2.isSatisfied(timestamp);
        traceIsSatisfied(timestamp, satisfied);
        return satisfied;
    }

    /**
     * @return the first rule
     */
    public Rule getRule1() {
        return rule1;
    }

    /**
     * @return the second rule
     */
    public Rule getRule2() {
        return rule2;
    }
}
