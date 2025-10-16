package com.gantenx.nethuns.commons.utils;

import com.gantenx.nethuns.commons.model.Candle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {
    public static List<Candle> getKLineList(String csvFile, long startTime, long endTime) {
        List<Candle> klineList = new ArrayList<>();
        try (InputStream is = CsvUtils.class.getClassLoader().getResourceAsStream(csvFile)) {
            assert is != null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    long timestamp = DateUtils.getTimestamp(values[0]);
                    if (timestamp < startTime || timestamp > endTime) {
                        continue;
                    }
                    Candle candle = new Candle(timestamp);
                    candle.setOpen(Double.parseDouble(values[1]));
                    candle.setHigh(Double.parseDouble(values[2]));
                    candle.setLow(Double.parseDouble(values[3]));
                    candle.setClose(Double.parseDouble(values[4]));
                    candle.setVolume(Double.parseDouble(values[6]));
                    klineList.add(candle);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return klineList;
    }

    public static List<Long> getOpenDayList(long startTime, long endTime) {
        List<Long> timestampList = new ArrayList<>();
        try (InputStream is = CsvUtils.class.getClassLoader().getResourceAsStream("data/OPEN_DAY.csv")) {
            assert is != null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                br.readLine();
                while ((line = br.readLine()) != null) {
                    long timestamp = DateUtils.getTimestamp(line.trim());
                    if (timestamp >= startTime && timestamp <= endTime) {
                        timestampList.add(timestamp);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return timestampList;
    }
}
