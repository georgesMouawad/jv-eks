package com.devops.crate.services.crate_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.devops.crate", "com.devops.common" })
public class CrateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrateServiceApplication.class, args);
    }
}
