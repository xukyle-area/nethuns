package com.gantenx.utils;

import java.lang.reflect.Type;

import com.google.gson.Gson;

public class JsonUtils {

    private static final Gson gson = new Gson();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String str, Class<T> cls) {
        return gson.fromJson(str, cls);
    }

    public static <T> T fromJson(String str, Type type) {
        return gson.fromJson(str, type);
    }

}
