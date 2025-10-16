package com.gantenx.nethuns.engine.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 时间管理器
 * 负责管理交易时间序列和当前交易时间点
 */
public class TimeManager {

    private final List<Long> timestamps;
    private int currentIndex = -1;

    /**
     * 构造器
     *
     * @param timestamps 时间戳列表
     */
    public TimeManager(List<Long> timestamps) {
        validateTimestamps(timestamps);
        this.timestamps = Collections.unmodifiableList(new ArrayList<>(timestamps));
    }

    /**
     * 是否还有下一个交易周期
     *
     * @return true如果还有下一个周期
     */
    public boolean hasNext() {
        return currentIndex + 1 < timestamps.size();
    }

    /**
     * 移动到下一个交易周期
     *
     * @return 下一个时间戳
     * @throws IllegalStateException 如果没有更多的交易周期
     */
    public long next() {
        if (!hasNext()) {
            throw new IllegalStateException("No more trading periods available");
        }
        currentIndex++;
        return timestamps.get(currentIndex);
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     * @throws IllegalStateException 如果交易尚未开始
     */
    public long getCurrentTimestamp() {
        if (currentIndex < 0) {
            throw new IllegalStateException("Trading not started. Call next() first.");
        }
        return timestamps.get(currentIndex);
    }

    /**
     * 重置到初始状态
     */
    public void reset() {
        currentIndex = -1;
    }

    /**
     * 获取总的交易周期数
     *
     * @return 总周期数
     */
    public int getTotalPeriods() {
        return timestamps.size();
    }

    /**
     * 获取当前进度百分比
     *
     * @return 进度百分比 (0-100)
     */
    public double getProgress() {
        if (currentIndex < 0) {
            return 0.0;
        }
        return (double) (currentIndex + 1) / timestamps.size() * 100;
    }

    /**
     * 验证时间戳列表
     */
    private void validateTimestamps(List<Long> timestamps) {
        if (timestamps == null) {
            throw new IllegalArgumentException("Timestamps cannot be null");
        }
        if (timestamps.isEmpty()) {
            throw new IllegalArgumentException("Timestamps cannot be empty");
        }

        // 验证时间戳是否按升序排列
        for (int i = 1; i < timestamps.size(); i++) {
            if (timestamps.get(i) <= timestamps.get(i - 1)) {
                throw new IllegalArgumentException("Timestamps must be in ascending order");
            }
        }
    }
}
