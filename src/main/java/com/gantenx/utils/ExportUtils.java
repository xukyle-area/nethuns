package com.gantenx.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;

@Slf4j
public class ExportUtils {

    public static void saveJFreeChartAsImage(JFreeChart chart, String strategyName, String filePrefix, int width, int height) {
        BufferedImage image = chart.createBufferedImage(width, height);
        File outputFile = new File(genChartPath(strategyName, filePrefix));

        try {
            ImageIO.write(image, "png", outputFile);
            log.info("Chart saved to: {}", outputFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chart as image.", e);
        }
    }

    private static String genChartPath(String strategyName, String filePrefix) {
        String timeWithoutDate = DateUtils.getTimeWithoutDate(System.currentTimeMillis(), ZoneOffset.ofHours(8));
        return "export/" + strategyName + "/" + filePrefix + "-" +
                timeWithoutDate +
                ".png";
    }

    public static void exportWorkbook(Workbook workbook, String strategyName, String filePrefix) {
        try {
            // 确保文件所在的目录存在
            File file = new File(genWorkbookPath(strategyName, filePrefix));
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

    private static String genWorkbookPath(String strategyName, String filePrefix) {
        String timeWithoutDate = DateUtils.getTimeWithoutDate(System.currentTimeMillis(), ZoneOffset.ofHours(8));
        return "export/" + strategyName + "/" + filePrefix + "-" +
                timeWithoutDate +
                ".xlsx";
    }
}
