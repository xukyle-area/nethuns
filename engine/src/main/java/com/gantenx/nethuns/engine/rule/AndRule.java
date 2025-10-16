package com.gantenx.nethuns.engine.rule;


/**
 * An AND combination of two {@link Rule rules}.
 *
 * <p>
 * Satisfied when both rules are satisfied.
 *
 * <p>
 * <b>Warning:</b> The second rule is not tested if the first rule is not
 * satisfied.
 */
public class AndRule extends AbstractRule {

    private final Rule rule1;
    private final Rule rule2;

    /**
     * Constructor.
     *
     * @param rule1 a trading rule
     * @param rule2 another trading rule
     */
    public AndRule(Rule rule1, Rule rule2) {
        this.rule1 = rule1;
        this.rule2 = rule2;
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

    @Override
    public boolean isSatisfied(long timestamp) {
        return false;
    }
}
