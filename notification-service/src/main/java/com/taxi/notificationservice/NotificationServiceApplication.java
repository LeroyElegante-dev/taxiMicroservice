package com.taxi.notificationservice;

import com.taxi.notificationservice.config.NotificationWorkerProperties;
import com.taxi.security.TaxiSecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(TaxiSecurityConfiguration.class)
@EnableConfigurationProperties(NotificationWorkerProperties.class)
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
