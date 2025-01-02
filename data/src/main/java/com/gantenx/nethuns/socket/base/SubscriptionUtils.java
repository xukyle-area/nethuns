package com.gantenx.nethuns.socket.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gantenx.nethuns.commons.constant.Market;
import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.socket.binance.BinanceRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public final class SubscriptionUtils {

    private SubscriptionUtils() {
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    private final static String BINANCE_SUBSCRIBE = "SUBSCRIBE";
    private static long id = 1L;

    public static String ofTickerSubscription(Market market, Set<Symbol> symbols) throws JsonProcessingException {
        log.info("build TickerSubscription market:{}, symbols:{}", market, symbols);
        if (market == Market.BINANCE) {
            List<String> params = SubscriptionUtils.buildParamsForBinance(symbols);
            BinanceRequest request = new BinanceRequest(BINANCE_SUBSCRIBE, params.toArray(new String[0]), id++);
            return mapper.writeValueAsString(request);
        } else {
            throw new RuntimeException("cannot convert the ticker subscription");
        }
    }

    public static String ofCandleSubscription(Market market, Set<Symbol> symbols) throws JsonProcessingException {
        if (market == Market.BINANCE) {
            try {
                List<String> params = SubscriptionUtils.buildParamsForBinance(symbols, Period.M_15);
                BinanceRequest request = new BinanceRequest(BINANCE_SUBSCRIBE, params.toArray(new String[0]), id++);
                return mapper.writeValueAsString(request);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Cannot convert the ticker subscription");
        }
    }

    private static List<String> buildParamsForBinance(Set<Symbol> symbols) {
        ArrayList<String> list = new ArrayList<>();
        for (Symbol s : symbols) {
            String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
            String x = symbol.toUpperCase() + "@ticker";
            list.add(x.toLowerCase());
        }
        return list;
    }

    private static List<String> buildParamForCrypto(Set<Symbol> symbols) {
        ArrayList<String> list = new ArrayList<>();
        for (Symbol s : symbols) {
            String x = "ticker." + s.getBase().getUpperName() + "_" + s.getQuote().getUpperName();
            list.add(x);
        }
        return list;
    }

    private static List<String> buildParamsForBinance(Set<Symbol> symbols, Period period) {
        ArrayList<String> list = new ArrayList<>();
        for (Symbol s : symbols) {
            String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
            String x = symbol.toUpperCase() + "@kline_" + period.getDesc();
            list.add(x.toLowerCase());
        }
        return list;
    }

    private static List<String> buildParamForCrypto(Set<Symbol> symbols, Period period) {
        ArrayList<String> list = new ArrayList<>();
        for (Symbol s : symbols) {
            String x = "ticker." + s.getBase().getUpperName() + "_" + s.getQuote().getUpperName();
            list.add(x);
        }
        return list;
    }
}
