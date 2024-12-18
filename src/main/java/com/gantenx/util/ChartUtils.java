package com.gantenx.util;

import com.gantenx.model.Kline;
import com.gantenx.model.Order;
import com.gantenx.model.TradingChart;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.RefineryUtilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class ChartUtils {

    public static TradingChart getTradingChart(List<Kline> qqqList, List<Kline> tqqqList, Map<Long, Double> rsiMap, List<Order> orderList) {
        return new TradingChart(qqqList, tqqqList, rsiMap, orderList);
    }

    public static void show(TradingChart chart) {
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }

    public static void saveJFreeChartAsImage(JFreeChart chart, String filePath, int width, int height) {
        BufferedImage image = chart.createBufferedImage(width, height);
        File outputFile = new File(filePath);

        try {
            ImageIO.write(image, "png", outputFile);
            log.info("Chart saved to: {}", outputFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chart as image.", e);
        }
    }
}
