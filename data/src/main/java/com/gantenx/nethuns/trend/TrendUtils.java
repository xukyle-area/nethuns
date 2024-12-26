package com.gantenx.nethuns.trend;


import com.gantenx.nethuns.commons.constant.Trend;

import java.util.*;

import static com.gantenx.nethuns.commons.constant.Trend.*;

public class TrendUtils {

    public static final List<Trend> STRONG_BUY_1 = Arrays.asList(STRONG_DOWNTREND, DOWNTREND, SIDEWAYS);
    public static final List<Trend> STRONG_BUY_2 = Arrays.asList(STRONG_DOWNTREND, SIDEWAYS);
    public static final List<Trend> STRONG_BUY_3 = Arrays.asList(DOWNTREND, SIDEWAYS);
    public static final List<Trend> STRONG_BUY_4 = Arrays.asList(DOWNTREND, STRONG_DOWNTREND);
    public static final List<Trend> STRONG_BUY_5 = Arrays.asList(STRONG_DOWNTREND, DOWNTREND);
    public static final List<Trend> STRONG_BUY_6 = Arrays.asList(DOWNTREND);
    public static final List<Trend> STRONG_SELL_1 = Arrays.asList(STRONG_UPTREND, UPTREND, SIDEWAYS);
    public static final List<Trend> STRONG_SELL_2 = Arrays.asList(STRONG_UPTREND, SIDEWAYS);
    public static final List<Trend> STRONG_SELL_3 = Arrays.asList(UPTREND, SIDEWAYS);

    public static boolean isStrongBuy(List<Trend> trendList) {
        if (match(trendList, STRONG_BUY_1)) {
            return true;
        }
        if (match(trendList, STRONG_BUY_2)) {
            return true;
        }
        if (match(trendList, STRONG_BUY_3)) {
            return true;
        }
        if (match(trendList, STRONG_BUY_4)) {
            return true;
        }
        if (match(trendList, STRONG_BUY_5)) {
            return true;
        }
        if (match(trendList, STRONG_BUY_6)) {
            return true;
        }
        return false;
    }

    public static boolean isStrongSell(List<Trend> trendList) {
        if (match(trendList, STRONG_SELL_1)) {
            return true;
        }
        if (match(trendList, STRONG_SELL_2)) {
            return true;
        }
        if (match(trendList, STRONG_SELL_3)) {
            return true;
        }
        return false;
    }

    /**
     * 检查趋势列表的末尾是否匹配模板序列（允许重复）
     *
     * @param trendList 完整的趋势列表
     * @param template  要匹配的模板序列
     * @return 是否匹配
     */
    public static boolean match(List<Trend> trendList, List<Trend> template) {
        if (template.size() > trendList.size()) {
            return false;
        }

        int listIndex = trendList.size() - 1;  // 从趋势列表末尾开始
        int templateIndex = template.size() - 1;  // 从模板末尾开始

        while (templateIndex >= 0 && listIndex >= 0) {
            Trend currentTrend = trendList.get(listIndex);
            Trend templateTrend = template.get(templateIndex);

            if (currentTrend.equals(templateTrend)) {
                // 匹配成功，继续匹配前一个
                listIndex--;
                templateIndex--;
            } else if (listIndex < trendList.size() - 1 && currentTrend.equals(trendList.get(listIndex + 1))) {
                // 当前趋势与前一个趋势相同，可以重复
                listIndex--;
            } else {
                return false;
            }
        }

        return templateIndex < 0;  // 所有模板元素都匹配成功
    }
}
