package com.gantenx.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 此处方法如果没有特殊说明，都指的是UTC时间，也就是标准时间
 */
public class DateUtils {
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static long getTimestamp(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr, formatter);
        return localDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    }

    public static String getDate(long timestamp) {
        LocalDate date = Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.UTC).toLocalDate();
        return date.format(formatter);
    }
}
