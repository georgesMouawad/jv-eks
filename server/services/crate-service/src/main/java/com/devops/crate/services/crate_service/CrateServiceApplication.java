package com.devops.crate.services.crate_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CrateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrateServiceApplication.class, args);
    }
}
