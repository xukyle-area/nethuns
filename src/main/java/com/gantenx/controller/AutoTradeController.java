package com.gantenx.controller;

import com.gantenx.model.Kline;
import com.gantenx.service.BinanceQuoteService;
import com.gantenx.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.gantenx.constant.Constants.ONE_DAY;

@Slf4j
@RestController
@RequestMapping("/auto-trade")
public class AutoTradeController {

    @Autowired
    private BinanceQuoteService binanceQuoteService;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/kline")
    public List<Kline> kline(@RequestParam("symbol") String symbol,
                             @RequestParam("begin") String beginStr,
                             @RequestParam("end") String endStr,
                             @RequestParam(value = "limit", required = false, defaultValue = "500") int limit) {
        return binanceQuoteService.getKline(symbol.toUpperCase(), ONE_DAY, DateUtils.getTimestamp(beginStr), DateUtils.getTimestamp(endStr), limit);
    }
}