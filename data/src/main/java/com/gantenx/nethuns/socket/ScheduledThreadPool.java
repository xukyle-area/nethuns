package com.gantenx.nethuns.socket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadPool {
    // 私有静态变量，用于保存定时调度线程池实例
    private static final ScheduledExecutorService scheduledExecutor;
    private static final int POOL_SIZE = 3;

    static{
        scheduledExecutor = Executors.newScheduledThreadPool(POOL_SIZE);
    }

    // 私有构造函数，防止外部实例化
    private ScheduledThreadPool() {
    }

    // 延迟执行任务
    public static void scheduleTask(Runnable task, long delay, TimeUnit unit) {
        scheduledExecutor.schedule(task, delay, unit);
    }

    public static void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static void scheduleWithFixedDelay(Runnable task, long initialDelay, long fixedDelay, TimeUnit unit) {
        scheduledExecutor.scheduleWithFixedDelay(task, initialDelay, fixedDelay, unit);
    }

    // 关闭定时调度线程池
    public static void shutdown() {
        scheduledExecutor.shutdown();
    }
}
