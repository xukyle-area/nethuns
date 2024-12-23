package com.gantenx.utils;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;

@Slf4j
public class ExportUtils {

    public static void saveJFreeChartAsImage(JFreeChart chart, String startStr, String endStr,
                                             String strategyName, String filePrefix) {
        if (chart == null) {
            throw new IllegalArgumentException("Chart cannot be null");
        }

        try {
            Path filePath = Paths.get(genChartPath(strategyName, startStr, endStr, filePrefix));
            Files.createDirectories(filePath.getParent());
            BufferedImage image = chart.createBufferedImage(2400, 1200);
            if (image == null) {
                throw new RuntimeException("Failed to create chart image");
            }
            ImageIO.write(image, "png", filePath.toFile());
            String absolutePath = filePath.toAbsolutePath().toString();
            log.info("Chart saved successfully to: {}", absolutePath);
        } catch (IOException e) {
            log.error("Failed to save chart as image. Strategy: {}, Period: {} to {}",
                      strategyName, startStr, endStr, e);
            throw new RuntimeException("Failed to save chart as image: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while saving chart. Strategy: {}", strategyName, e);
            throw new RuntimeException("Unexpected error while saving chart: " + e.getMessage(), e);
        }
    }


    public static void exportWorkbook(Workbook workbook, String startStr, String endStr, String strategyName, String filename) {
        try {
            // 确保文件所在的目录存在
            File file = new File(genWorkbookPath(strategyName, startStr, endStr, filename));
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean dirsCreated = parentDir.mkdirs();
                if (!dirsCreated) {
                    throw new IOException("Failed to create directories for export path: " + parentDir.getAbsolutePath());
                }
            }

            // 写入文件
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            } finally {
                workbook.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export workbook.", e);
        }
    }

    private static final Joiner joiner = Joiner.on("/");

    private static String genChartPath(String strategyName, String startStr, String endStr, String filename) {
        String timeWithoutDate = DateUtils.getDateTimeForExport(System.currentTimeMillis(), ZoneOffset.ofHours(8));
        String timeRange = startStr + "-" + endStr;
        String fullName = strategyName + "-" + filename + ".png";
        return joiner.join("export", timeWithoutDate, timeRange, fullName);
    }

    private static String genWorkbookPath(String strategyName, String startStr, String endStr, String filename) {
        String timeWithoutDate = DateUtils.getDateTimeForExport(System.currentTimeMillis(), ZoneOffset.ofHours(8));
        String timeRange = startStr + "-" + endStr;
        String fullName = strategyName + "-" + filename + ".xlsx";
        return joiner.join("export", timeWithoutDate, timeRange, fullName);
    }
}
