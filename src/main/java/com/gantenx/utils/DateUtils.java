package com.gantenx.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 此处方法如果没有特殊说明，都指的是UTC时间，也就是标准时间
 */
public class DateUtils {
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT_WITHOUT_TIME = new SimpleDateFormat("dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER_WITHOUT_DATE = DateTimeFormatter.ofPattern("HH:mm");

    public static final Long MS_OF_ONE_DAY = 1000L * 3600 * 24;
    public static final double MS_OF_ONE_DAY_DOUBLE = 1000L * 3600 * 24.0;

    public static long getTimestamp(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr, DATE_TIME_FORMATTER);
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();  // 使用毫秒时间戳
    }

    public static String getDate(long timestamp) {
        LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDate();  // 使用毫秒时间戳
        return date.format(DATE_TIME_FORMATTER);
    }

    /**
     * 获取指定时间戳的时分秒字符串
     *
     * @param timestamp  毫秒时间戳
     * @param zoneOffset 时区偏移
     * @return 格式化的时分秒字符串 (HHmmss)
     */
    public static String getTimeWithoutDate(long timestamp, ZoneOffset zoneOffset) {
        LocalTime time = Instant.ofEpochMilli(timestamp)
                .atZone(zoneOffset)
                .toLocalTime();
        return time.format(DATE_TIME_FORMATTER_WITHOUT_DATE);
    }
}
