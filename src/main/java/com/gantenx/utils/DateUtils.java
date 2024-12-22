package com.gantenx.utils;

import com.gantenx.constant.Period;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 此处方法如果没有特殊说明，都指的是UTC时间，也就是标准时间
 */
public class DateUtils {
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final static DateTimeFormatter DATE_TIME_ORDER_MARKER = DateTimeFormatter.ofPattern("MMdd");
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd/HH:mm");
    public static final double MS_OF_ONE_DAY_DOUBLE = 1000L * 3600 * 24.0;

    public static long getTimestamp(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr, DATE_TIME_FORMATTER);
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();  // 使用毫秒时间戳
    }

    public static long getDaysBetween(long before, long after) {
        return (after - before) / Period.ONE_DAY.getMillisecond();
    }

    public static List<Long> genTimeList(Period period, long startTimestamp, long endTimestamp) {
        List<Long> list = new ArrayList<>();
        startTimestamp = startTimestamp - startTimestamp % period.getMillisecond();
        for (long i = startTimestamp; i <= endTimestamp; i += period.getMillisecond()) {
            list.add(i);
        }
        return list;
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

    public static String getDate(long timestamp) {
        LocalDate date = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDate();  // 使用毫秒时间戳
        return date.format(DATE_TIME_FORMATTER);
    }
}
