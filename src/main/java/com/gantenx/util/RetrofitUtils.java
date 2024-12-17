package com.gantenx.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class RetrofitUtils {

    public static <T, R> CompletableFuture<R> enqueueRequest(
            Call<T> call,
            Function<T, R> successHandler,
            Consumer<String> errorHandler) {

        CompletableFuture<R> future = new CompletableFuture<>();

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response) {
                T body = response.body();
                if (response.isSuccessful() && body != null) {
                    try {
                        // 使用提供的转换器将响应数据转换为目标类型
                        log.info("请求结果:{}", body);
                        R result = successHandler.apply(body);
                        future.complete(result);  // 返回转换后的结果
                    } catch (Exception e) {
                        future.completeExceptionally(new RuntimeException("转换失败", e));
                    }
                } else {
                    future.completeExceptionally(new RuntimeException("请求失败，状态码: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NotNull Call<T> call, @NotNull Throwable t) {
                errorHandler.accept(t.getMessage());
                future.completeExceptionally(t);  // 请求错误，返回异常
            }
        });

        return future;
    }
}
