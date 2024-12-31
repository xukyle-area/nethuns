package com.gantenx.nethuns.commons.constant;

public enum Period {
    // public static final String ONE_DAY = "1d";
    CSV("csv", 1000L * 3600 * 24),
    D_1("1d", 1000L * 3600 * 24),
    H_4("4h", 1000L * 3600 * 4),
    H_1("1h", 1000L * 3600),
    M_1("1m", 1000L * 60),
    M_15("15m", 1000L * 60 * 15),
    M_30("30m", 1000L * 60 * 30);
    private final String desc;
    private final long millisecond;

    Period(String desc, long millisecond) {
        this.desc = desc;
        this.millisecond = millisecond;
    }

    public String getDesc() {
        return desc;
    }

    public long getMillisecond() {
        return millisecond;
    }

    public static Period getPeriod(long time) {
        for (Period value : Period.values()) {
            if (value.getMillisecond() == time) {
                return value;
            }
        }
        throw new RuntimeException();
    }
}
