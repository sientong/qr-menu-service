package com.qrmenu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.qrmenu")
@EnableCaching
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.qrmenu.repository")
@EntityScan(basePackages = "com.qrmenu.model")
public class QrMenuApplication {
    public static void main(String[] args) {
        SpringApplication.run(QrMenuApplication.class, args);
    }

} 