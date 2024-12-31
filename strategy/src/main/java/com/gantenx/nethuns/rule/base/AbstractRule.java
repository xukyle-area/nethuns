package com.gantenx.nethuns.rule.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract trading {@link Rule rule}.
 */
public abstract class AbstractRule implements Rule {

    /** The logger */
    protected final transient Logger log = LoggerFactory.getLogger(getClass());

    /** The class name */
    private final String className = getClass().getSimpleName();

    /**
     * Traces the {@code isSatisfied()} method calls.
     *
     * @param index       the bar index
     * @param isSatisfied true if the rule is satisfied, false otherwise
     */
    protected void traceIsSatisfied(long index, boolean isSatisfied) {
        if (log.isTraceEnabled()) {
            log.trace("{}#isSatisfied({}): {}", className, index, isSatisfied);
        }
    }

    public abstract boolean isSatisfied(long timestamp);
}
