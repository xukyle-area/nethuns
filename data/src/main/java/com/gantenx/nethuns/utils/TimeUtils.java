package com.gantenx.nethuns.utils;

import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.utils.CsvUtils;

import java.util.ArrayList;
import java.util.List;

public class TimeUtils {
    public static List<Long> genTimeList(Period period, long startTimestamp, long endTimestamp) {
        startTimestamp = startTimestamp - startTimestamp % period.getMillisecond();
        if (Period.CSV.equals(period)) {
            return CsvUtils.getOpenDayList(startTimestamp, endTimestamp);
        }
        List<Long> list = new ArrayList<>();
        for (long i = startTimestamp; i <= endTimestamp; i += period.getMillisecond()) {
            list.add(i);
        }
        return list;
    }
}
