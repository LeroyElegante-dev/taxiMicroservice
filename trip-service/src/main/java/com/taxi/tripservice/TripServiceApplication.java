package com.taxi.tripservice;

import com.taxi.security.TaxiSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.taxi.tripservice")
@EnableFeignClients(basePackages = "com.taxi.tripservice.integration.feign")
@Import(TaxiSecurityConfiguration.class)
public class TripServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripServiceApplication.class, args);
    }
}
