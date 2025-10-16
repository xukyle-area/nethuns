package com.gantenx.nethuns.engine.exception;

/**
 * 导出异常
 * 结果导出过程中发生的异常
 */
public class ExportException extends RuntimeException {

    public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportException(Throwable cause) {
        super(cause);
    }
}
