package com.gantenx.nethuns.engine.chart;

import com.gantenx.nethuns.commons.utils.DateUtils;
import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;

import static com.gantenx.nethuns.commons.constant.Constants.joiner;

public class ExportUtils {

    public static void saveJFreeChartAsImage(JFreeChart chart, String strategyName) {
        if (chart == null) {
            return;
        }

        try {
            Path filePath = Paths.get(genChartPath(strategyName));
            Files.createDirectories(filePath.getParent());
            BufferedImage image = chart.createBufferedImage(3600, 1200);
            if (image == null) {
                throw new RuntimeException("Failed to create chart image");
            }
            ImageIO.write(image, "png", filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chart as image: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while saving chart: " + e.getMessage(), e);
        }
    }

    private static String genChartPath(String strategyName) {
        String timeWithoutDate = DateUtils.getDateTimeForExport(System.currentTimeMillis(), ZoneOffset.ofHours(8));
        String fullName = strategyName + ".png";
        return joiner.join("alpha/export", timeWithoutDate, fullName);
    }
}
