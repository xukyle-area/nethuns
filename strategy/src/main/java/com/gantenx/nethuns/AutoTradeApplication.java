package com.gantenx.nethuns;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.gantenx"})
public class AutoTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoTradeApplication.class);
    }
}
