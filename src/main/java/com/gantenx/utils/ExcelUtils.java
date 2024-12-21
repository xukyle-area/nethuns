package com.gantenx.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gantenx.annotation.ExcelColumn;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class ExcelUtils {

    private static final ObjectMapper objectMapper = configureObjectMapper();

    private static ObjectMapper configureObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();

        // 配置Double序列化，保留3位小数
        module.addSerializer(Double.class, new JsonSerializer<Double>() {
            @Override
            public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value != null) {
                    gen.writeNumber(new BigDecimal(value).setScale(3, RoundingMode.HALF_UP));
                }
            }
        });

        // 配置枚举序列化，使用name()
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifyEnumSerializer(SerializationConfig config,
                                                          JavaType valueType,
                                                          BeanDescription beanDesc,
                                                          JsonSerializer<?> serializer) {
                return new JsonSerializer<Enum<?>>() {
                    @Override
                    public void serialize(Enum<?> value,
                                          JsonGenerator gen,
                                          SerializerProvider serializers) throws IOException {
                        if (value != null) {
                            gen.writeString(value.name());
                        }
                    }
                };
            }
        });

        mapper.registerModule(module);

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
            return;
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
                        } else if (field.getType().isEnum()) {
                            // 处理枚举类型，保存 .name()
                            cell.setCellValue(((Enum<?>) value).name());
                        } else if (field.getType() == Double.class || field.getType() == double.class ||
                                field.getType() == Float.class || field.getType() == float.class) {
                            // 处理小数，保留两位有效小数
                            cell.setCellValue(String.format("%.3f", (double) value));
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

        // 递归获取所有父类
        List<Class<?>> classes = new ArrayList<>();
        while (clazz != null) {
            classes.add(clazz);
            clazz = clazz.getSuperclass();
        }

        // 反转类列表，从父类开始处理
        Collections.reverse(classes);

        // 按照父类到子类的顺序添加字段
        for (Class<?> cls : classes) {
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
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
