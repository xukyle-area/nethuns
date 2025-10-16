package com.gantenx.nethuns.engine.rule;

public class NotRule extends AbstractRule {

    private final Rule ruleToNegate;

    public NotRule(Rule ruleToNegate) {
        this.ruleToNegate = ruleToNegate;
    }

    @Override
    public boolean isSatisfied(long timestamp) {
        final boolean satisfied = !ruleToNegate.isSatisfied(timestamp);
        traceIsSatisfied(timestamp, satisfied);
        return satisfied;
    }

    public Rule getRuleToNegate() {
        return ruleToNegate;
    }
}
