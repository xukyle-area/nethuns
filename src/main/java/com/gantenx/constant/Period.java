package com.gantenx.constant;

public enum Period {
    // public static final String ONE_DAY = "1d";
    ONE_DAY("1d", 1000L * 3600 * 24),
    FOUR_HOURS("4h", 1000L * 3600 * 4);
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
}
