package com.gantenx.nethuns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.gantenx.nethuns.commons.utils.ConfigValidator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.gantenx"})
public class AutoTradeApplication {
    public static void main(String[] args) {
        log.info("=== Nethuns 自动交易系统启动中 ===");

        // 在应用启动前验证配置
        ConfigValidator.validateAllConfigs();

        SpringApplication.run(AutoTradeApplication.class, args);

        log.info("=== Nethuns 自动交易系统启动完成 ===");
        log.info("访问健康检查: http://localhost:8080/actuator/health");
    }
}
