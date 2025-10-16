package com.gantenx.nethuns.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/auto-trade")
public class AutoTradeController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    // @GetMapping("/kline")
    // public Map<Long, Kline> kline(@RequestParam("base") Currency base,
    // @RequestParam("quote") Currency quote,
    // @RequestParam("start") String startStr,
    // @RequestParam("end") String endStr) {
    // Symbol symbol = Symbol.toSymbol(base, quote);
    // return KlineService.getKLineMap(symbol,
    // D_1,
    // DateUtils.getTimestamp(startStr),
    // DateUtils.getTimestamp(endStr));
    // }
}
