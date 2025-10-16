package com.gantenx.nethuns.utils;

import java.util.ArrayList;
import java.util.List;
import com.gantenx.nethuns.commons.enums.Period;
import com.gantenx.nethuns.commons.utils.CsvUtils;
import com.gantenx.nethuns.commons.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeUtils {
    public static List<Long> genTimeList(Period period, long startTimestamp, long endTimestamp) {
        startTimestamp = startTimestamp - startTimestamp % period.getMillisecond();
        if (Period.CSV.equals(period)) {
            return CsvUtils.getOpenDayList(startTimestamp, endTimestamp);
        }
        List<Long> list = new ArrayList<>();
        int count = 0;
        long end = 0L;
        for (long i = startTimestamp; i <= endTimestamp && count < 1000; i += period.getMillisecond()) {
            list.add(i);
            end = i;
            count++;
        }
        log.info("Time range: from {} to {}", DateUtils.getDateTime(startTimestamp), DateUtils.getDateTime(end));
        return list;
    }
}
