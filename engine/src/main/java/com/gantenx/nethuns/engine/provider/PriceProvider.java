package com.gantenx.nethuns.engine.provider;

import com.gantenx.nethuns.commons.enums.Symbol;

/**
 * 价格提供器接口
 * 定义获取交易标的价格的方法
 */
public interface PriceProvider {

    /**
     * 获取指定标的在指定时间的价格
     *
     * @param symbol    交易标的
     * @param timestamp 时间戳
     * @return 价格
     * @throws IllegalArgumentException 如果没有找到对应的价格数据
     */
    double getPrice(Symbol symbol, long timestamp);

    /**
     * 检查是否有指定标的的价格数据
     *
     * @param symbol 交易标的
     * @return true如果有价格数据
     */
    boolean hasPrice(Symbol symbol);

    /**
     * 检查是否有指定标的在指定时间的价格数据
     *
     * @param symbol    交易标的
     * @param timestamp 时间戳
     * @return true如果有价格数据
     */
    boolean hasPrice(Symbol symbol, long timestamp);
}
