package com.gantenx.controller;

import com.gantenx.constant.Currency;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.service.KlineService;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auto-trade")
public class AutoTradeController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/kline")
    public Map<Long, Kline> kline(@RequestParam("base") Currency base,
                                  @RequestParam("quote") Currency quote,
                                  @RequestParam("start") String startStr,
                                  @RequestParam("end") String endStr) {
        Symbol symbol = Symbol.toSymbol(base, quote);
        return KlineService.getKLineMap(symbol, DateUtils.getTimestamp(startStr), DateUtils.getTimestamp(endStr));
    }
}