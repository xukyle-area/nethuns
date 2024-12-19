package com.gantenx.utils;

import com.gantenx.annotation.ExcelColumn;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ExcelUtils {

    /**
     * 生成一个包含一个 sheet 的工作簿
     *
     * @param dataList  数据列表
     * @param sheetName sheet 的名称
     * @param <T>       数据类型的枚举
     * @return 包含一个 sheet 的工作簿
     */
    public static <T> Workbook singleSheet(List<T> dataList, String sheetName) {
        Workbook workbook = ExcelUtils.createWorkbook();
        ExcelUtils.addDataToNewSheet(workbook, dataList, sheetName);
        return workbook;
    }

    /**
     * 添加数据到工作表的新sheet
     *
     * @param workbook  工作簿
     * @param dataList  数据列表
     * @param sheetName sheet名称
     * @param <T>       数据类型
     */
    public static <T> void addDataToNewSheet(Workbook workbook, List<T> dataList, String sheetName) {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("The data list is empty or null.");
        }

        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);

        // 获取所有字段，包括父类字段
        List<Field> fields = getAllFields(dataList.get(0).getClass());

        int headerIndex = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            String headerName = field.getName();
            if (Objects.nonNull(excelColumn)) {
                if (!excelColumn.need()) {
                    continue;
                }
                if (!Strings.isNullOrEmpty(excelColumn.name())) {
                    headerName = excelColumn.name();
                }
            }
            Cell cell = headerRow.createCell(headerIndex++);
            cell.setCellValue(headerName);
            cell.setCellStyle(createHeaderCellStyle(workbook));
        }

        // Fill data rows
        int rowNum = 1;
        for (T item : dataList) {
            Row row = sheet.createRow(rowNum++);
            headerIndex = 0;
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
                    if (Objects.nonNull(excelColumn) && !excelColumn.need()) {
                        continue;
                    }

                    Cell cell = row.createCell(headerIndex++);
                    Object value = field.get(item);
                    if (value != null) {
                        if (excelColumn != null && !excelColumn.dateFormat().isEmpty() &&
                                (field.getType() == Long.class || field.getType() == long.class)) {
                            // Format the value as a date
                            String formattedValue = formatDate((Long) value, excelColumn.dateFormat());
                            cell.setCellValue(formattedValue);
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field value.", e);
                }
            }
        }
    }

    // Helper method to recursively get all fields, including from superclasses
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    // Helper method to format date
    private static String formatDate(long timestamp, String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDate().format(formatter);
    }


    /**
     * @param response 来自 controller
     * @param workbook 需要下载的 workbook 对象
     * @param filename 保存下来的文件名
     */
    public static void downloadExcel(HttpServletResponse response, Workbook workbook, String filename) {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            try (InputStream inputStream = new ByteArrayInputStream(baos.toByteArray())) {
                IOUtils.copy(inputStream, response.getOutputStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("Download file error.", e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                log.error("Error closing workbook", e);
            }
        }
    }

    public static Workbook createWorkbook() {
        return new XSSFWorkbook();
    }


    /**
     * 创建表头样式
     *
     * @param workbook 工作簿
     * @return 表头样式
     */
    private static CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
