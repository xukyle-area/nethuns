package com.gantenx.utils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 此处方法如果没有特殊说明，都指的是UTC时间，也就是标准时间
 */
public class DateUtils {
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT_WITHOUT_TIME = new SimpleDateFormat("dd");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm");

    public static final Long MS_OF_ONE_DAY = 1000L * 3600 * 24;
    public static final double MS_OF_ONE_DAY_DOUBLE = 1000L * 3600 * 24.0;

    public static long getTimestamp(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr, DATE_TIME_FORMATTER);
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();  // 使用毫秒时间戳
    }

    public static long getDaysBetween(long before, long after) {
        return (after - before) / MS_OF_ONE_DAY;
    }

    /**
     * 带毫秒的时间格式
     */
    public static String getDateTimeForExport(long timestamp, ZoneOffset zoneOffset) {
        long epochSecond = timestamp / 1000;
        int nanoOfSecond = (int) (timestamp % 1000) * 1_000_000;
        return LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, zoneOffset).format(FORMATTER);
    }
}
