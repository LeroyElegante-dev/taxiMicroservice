package com.taxi.tripservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.taxi.tripservice")
@EnableFeignClients(basePackages = "com.taxi.tripservice.integration.feign")
public class TripServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripServiceApplication.class, args);
    }
}
