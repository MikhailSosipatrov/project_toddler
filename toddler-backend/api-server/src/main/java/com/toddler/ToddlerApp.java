package com.toddler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ToddlerApp {
    public static void main(String[] args) {
        SpringApplication.run(ToddlerApp.class, args);
    }
}
