package com.gantenx.utils;

import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class CsvUtils {
    public static List<Kline> getKLineFromCsv(String csvFile, long startTime, long endTime) {
        List<Kline> klineList = new ArrayList<>();

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
                    Kline kline = new Kline(timestamp);
                    kline.setOpen(Double.parseDouble(values[1]));
                    kline.setHigh(Double.parseDouble(values[2]));
                    kline.setLow(Double.parseDouble(values[3]));
                    kline.setClose(Double.parseDouble(values[4]));
                    kline.setVolume(Double.parseDouble(values[6]));
                    klineList.add(kline);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return klineList;
    }

    public static Map<Long, Kline> getKLineMap(Symbol symbol, long startTime, long endTime) {
        List<Kline> qqqKlineList = CsvUtils.getKLineFromCsv(symbol.getPath(), startTime, endTime);
        return CollectionUtils.toTimeMap(qqqKlineList);
    }
}
