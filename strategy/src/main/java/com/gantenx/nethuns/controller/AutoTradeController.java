package com.gantenx.nethuns.controller;


import com.gantenx.nethuns.commons.constant.Currency;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.service.KlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.gantenx.nethuns.commons.constant.Period.D_1;


@Slf4j
@RestController
@RequestMapping("/auto-trade")
public class AutoTradeController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

//    @GetMapping("/kline")
//    public Map<Long, Kline> kline(@RequestParam("base") Currency base,
//                                  @RequestParam("quote") Currency quote,
//                                  @RequestParam("start") String startStr,
//                                  @RequestParam("end") String endStr) {
//        Symbol symbol = Symbol.toSymbol(base, quote);
//        return KlineService.getKLineMap(symbol,
//                                        D_1,
//                                        DateUtils.getTimestamp(startStr),
//                                        DateUtils.getTimestamp(endStr));
//    }
}