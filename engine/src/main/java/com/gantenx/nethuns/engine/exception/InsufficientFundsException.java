package com.gantenx.nethuns.engine.exception;

/**
 * 余额不足异常
 * 当尝试执行需要资金的操作但账户余额不足时抛出
 */
public class InsufficientFundsException extends RuntimeException {

    /**
     * 构造器
     *
     * @param message 异常消息
     */
    public InsufficientFundsException(String message) {
        super(message);
    }

    /**
     * 构造器
     *
     * @param message 异常消息
     * @param cause   原因异常
     */
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
