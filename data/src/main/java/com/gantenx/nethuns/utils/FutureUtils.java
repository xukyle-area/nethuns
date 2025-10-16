package com.gantenx.nethuns.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import com.gantenx.nethuns.converter.Converter;
import retrofit2.Call;

public class FutureUtils {

    public static final Consumer<String> NO_OP_CONSUMER = o -> {
    };

    public static <T> T get(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T get(Call<T> call) {
        return FutureUtils.get(RetrofitUtils.enqueueRequest(call, Converter::nonOperation, NO_OP_CONSUMER));
    }

}
