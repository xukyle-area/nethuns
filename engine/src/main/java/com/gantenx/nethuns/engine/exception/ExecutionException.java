package com.gantenx.nethuns.engine.exception;

/**
 * 执行异常
 * 交易执行过程中发生的异常
 */
public class ExecutionException extends RuntimeException {

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutionException(Throwable cause) {
        super(cause);
    }
}
