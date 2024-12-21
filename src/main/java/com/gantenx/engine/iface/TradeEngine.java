package com.gantenx.engine.iface;

import com.gantenx.engine.Position;
import com.gantenx.engine.TradeDetail;
import com.gantenx.model.Kline;

import java.util.List;
import java.util.Map;

public interface TradeEngine<T> {

    /**
     * 判断是否存在任何持仓
     */
    boolean hasPosition();

    /**
     * 按照 balance 比例买入
     *
     * @param proportion 买入比例，范围为 0 到 100
     */
    void buy(T symbol, double price, long proportion, long timestamp, String reason);

    /**
     * 按照比例卖出
     *
     * @param proportion 卖出比例，范围为 0 到 100
     */
    void sell(T symbol, double price, long proportion, long timestamp, String reason);

    /**
     * 获取某个标的的持仓数量
     *
     * @param symbol 标的符号
     * @return 持有的数量
     */
    double getQuantity(T symbol);

    /**
     * 获取某个标的的持仓具体详情
     *
     * @param symbol 标的符号
     * @return 分别买入的时间和数量
     */
    List<Position> getPositions(T symbol);

    /**
     * 获取余额
     */
    double getBalance();

    /**
     * 获取现有的所有的持仓的资产
     *
     * @param priceMap 当前各个标的的价格
     */
    double getPositionAsset(Map<T, Kline> priceMap);

    /**
     * 结束交易
     */
    TradeDetail<T> exit(Map<T, Kline> priceMap, long timestamp);
}
