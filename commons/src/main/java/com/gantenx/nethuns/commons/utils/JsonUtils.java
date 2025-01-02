package com.gantenx.nethuns.commons.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

public class JsonUtils {

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
     * Convert an object to its JSON string representation.
     *
     * @param obj the object to convert
     * @return the JSON string
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    /**
     * Convert an object to its JSON string representation (alias of toJson).
     *
     * @param obj the object to serialize
     * @return the JSON string
     */
    public static String writeValueAsString(Object obj) {
        return toJson(obj);
    }

    /**
     * Convert a JSON string to an object of the specified class.
     *
     * @param str the JSON string
     * @param cls the target class
     * @param <T> the type of the object
     * @return the deserialized object
     */
    public static <T> T fromJson(String str, Class<T> cls) {
        return readValue(str, cls);
    }

    /**
     * Convert a JSON string to an object with a generic type.
     *
     * @param str the JSON string
     * @param typeReference the type reference representing the target type
     * @param <T> the type of the object
     * @return the deserialized object
     */
    public static <T> T fromJson(String str, TypeReference<T> typeReference) {
        return readValue(str, typeReference);
    }

    /**
     * Deserialize JSON string to a class type.
     *
     * @param str the JSON string
     * @param cls the target class
     * @param <T> the type of the object
     * @return the deserialized object
     */
    public static <T> T readValue(String str, Class<T> cls) {
        try {
            return objectMapper.readValue(str, cls);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON to object", e);
        }
    }

    /**
     * Deserialize JSON string to a generic type.
     *
     * @param str the JSON string
     * @param typeReference the type reference representing the target type
     * @param <T> the type of the object
     * @return the deserialized object
     */
    public static <T> T readValue(String str, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(str, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON to object with TypeReference", e);
        }
    }
}
