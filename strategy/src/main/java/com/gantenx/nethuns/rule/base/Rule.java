package com.gantenx.nethuns.rule.base;

public interface Rule {

    default Rule and(Rule rule) {
        return new AndRule(this, rule);
    }

    default Rule or(Rule rule) {
        return new OrRule(this, rule);
    }

    default Rule xor(Rule rule) {
        return new XorRule(this, rule);
    }

    default Rule negation() {
        return new NotRule(this);
    }

    boolean isSatisfied(long timestamp);
}
