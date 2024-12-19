package com.gantenx.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FutureUtils {
    public static <T> T get(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
