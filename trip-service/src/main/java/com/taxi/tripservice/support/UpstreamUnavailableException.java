package com.taxi.tripservice.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class UpstreamUnavailableException extends RuntimeException {

    public UpstreamUnavailableException(String message) {
        super(message);
    }

    public UpstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
