package com.gantenx.util;

import com.gantenx.model.Kline;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvUtils {
    public static List<Kline> getKLineFromCsv(String csvFile, String start, String end) {
        long startTime = DateUtils.getTimestamp(start);
        long endTime = DateUtils.getTimestamp(end);
        List<Kline> klineList = new ArrayList<>();

        try (InputStream is = CsvUtils.class.getClassLoader().getResourceAsStream(csvFile)) {
            assert is != null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    Kline kline = new Kline();
                    long timestamp = DateUtils.getTimestamp(values[0]);
                    if (timestamp < startTime || timestamp > endTime) {
                        continue;
                    }
                    kline.setTime(timestamp);
                    kline.setOpen(values[1]);
                    kline.setHigh(values[2]);
                    kline.setLow(values[3]);
                    kline.setClose(values[4]);
                    kline.setVolume(values[6]);
                    klineList.add(kline);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return klineList;
    }
}
