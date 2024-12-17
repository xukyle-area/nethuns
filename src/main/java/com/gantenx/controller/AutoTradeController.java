package com.gantenx.controller;

import com.gantenx.model.response.KlineModel;
import com.gantenx.service.QuoteService;
import com.gantenx.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auto-trade")
public class AutoTradeController {

    @Autowired
    private QuoteService quoteService;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/btcusdt")
    public List<KlineModel> btcusdt() {
        long begin = DateUtils.getTimestamp("20241101");
        long end = DateUtils.getTimestamp("20241201");
        log.info("{}, {}", begin, end);
        return quoteService.getKline("BTCUSDT", "1d", begin, end, 500);
    }
}