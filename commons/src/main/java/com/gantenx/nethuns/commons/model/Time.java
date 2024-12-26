package com.gantenx.nethuns.commons.model;


import com.gantenx.nethuns.commons.annotation.ExcelColumn;

public class Time {

    @ExcelColumn(name = "date", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private long timestamp;

    public Time(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
