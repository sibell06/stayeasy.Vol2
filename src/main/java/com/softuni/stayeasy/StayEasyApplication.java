package com.softuni.stayeasy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class StayEasyApplication {

    public static void main(String[] args) {
        SpringApplication.run(StayEasyApplication.class, args);
    }
}