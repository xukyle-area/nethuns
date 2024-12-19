package com.gantenx.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gantenx.annotation.ExcelColumn;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class ExcelUtils {

    private static final ObjectMapper objectMapper = configureObjectMapper();

    // 如果需要更多自定义配置
    private static ObjectMapper configureObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 设置日期格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 忽略空bean错误
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 设置时间格式为ISO-8601
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 注册模块（如果需要）
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }

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
                            // 处理日期格式
                            String formattedValue = formatDate((Long) value, excelColumn.dateFormat());
                            cell.setCellValue(formattedValue);
                        } else if (field.getType().isArray() ||
                                isCollectionOrMap(field.getType()) ||
                                isCustomObject(field.getType())) {
                            // 使用JSON序列化处理数组、集合、Map和自定义对象
                            try {
                                String jsonValue = objectMapper.writeValueAsString(value);
                                cell.setCellValue(jsonValue);
                            } catch (JsonProcessingException e) {
                                cell.setCellValue("Error: Failed to serialize to JSON");
                                log.error("Failed to serialize field {} to JSON", field.getName(), e);
                            }
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


    // 辅助方法：检查是否为集合或Map类型
    private static boolean isCollectionOrMap(Class<?> type) {
        return Collection.class.isAssignableFrom(type) ||
                Map.class.isAssignableFrom(type);
    }

    // 辅助方法：检查是否为自定义对象（非基本类型和包装类）
    private static boolean isCustomObject(Class<?> type) {
        return !type.isPrimitive() &&
                !type.getName().startsWith("java.lang") &&
                !type.getName().startsWith("java.time") &&
                !Number.class.isAssignableFrom(type);
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
