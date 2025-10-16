package com.gantenx.nethuns.commons.utils;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import com.gantenx.nethuns.commons.enums.Period;

/**
 * 此处方法如果没有特殊说明，都指的是UTC时间，也就是标准时间
 */
public class DateUtils {
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyyMMdd";
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(YYYY_MM_DD);
    private final static DateTimeFormatter DATE_TIME_ORDER_MARKER = DateTimeFormatter.ofPattern("MMdd");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm");

    public static long getTimestamp(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr, DATE_TIME_FORMATTER);
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();  // 使用毫秒时间戳
    }

    public static long getDaysBetween(long before, long after) {
        return (after - before) / Period.D_1.getMillisecond();
    }

    public static String getDateTimeForExport(long timestamp, ZoneOffset zoneOffset) {
        long epochSecond = timestamp / 1000;
        int nanoOfSecond = (int) (timestamp % 1000) * 1_000_000;
        return LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, zoneOffset).format(FORMATTER);
    }

    public static String getDateForOrderMarker(long timestamp) {
        LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDate();  // 使用毫秒时间戳
        return date.format(DATE_TIME_ORDER_MARKER);
    }

    public static String getDateTime(long timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
        return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDateTime().format(formatter);
    }

    public static String getDate(long timestamp) {
        LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDate();  // 使用毫秒时间戳
        return date.format(DATE_TIME_FORMATTER);
    }
}
