package com.taxi.tripservice.integration.feign;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignNotificationAuthConfig {

    @Bean
    public RequestInterceptor notificationAuthForwardingInterceptor() {
        return template -> {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletRequestAttributes) {
                String auth = servletRequestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (auth != null && !auth.isBlank()) {
                    template.header(HttpHeaders.AUTHORIZATION, auth);
                }
            }
        };
    }
}
