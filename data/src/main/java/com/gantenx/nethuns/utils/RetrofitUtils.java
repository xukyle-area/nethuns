package com.gantenx.nethuns.utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class RetrofitUtils {

    public static <T, R> CompletableFuture<R> enqueueRequest(
            Call<T> call,
            Function<T, R> successHandler,
            Consumer<String> errorHandler) {

        CompletableFuture<R> future = new CompletableFuture<>();

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                T body = response.body();
                if (response.isSuccessful() && body != null) {
                    try {
                        R result = successHandler.apply(body);
                        future.complete(result);
                    } catch (Exception e) {
                        future.completeExceptionally(new RuntimeException("转换失败", e));
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("请求失败，状态码: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                errorHandler.accept(t.getMessage());
                future.completeExceptionally(t);
            }
        });

        return future;
    }
}
