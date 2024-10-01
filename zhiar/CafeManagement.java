package com.zhiar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(scanBasePackages = "com.zhiar" , exclude = { SecurityAutoConfiguration.class })
public class CafeManagement {

    public static void main(String[] args) {
        SpringApplication.run(CafeManagement.class, args);

    }
}