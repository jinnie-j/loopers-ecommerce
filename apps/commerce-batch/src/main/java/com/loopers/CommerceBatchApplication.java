package com.loopers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.loopers")
public class CommerceBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommerceBatchApplication.class, args);
    }

}
