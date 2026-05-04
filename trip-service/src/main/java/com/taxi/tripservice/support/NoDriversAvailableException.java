package com.taxi.tripservice.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class NoDriversAvailableException extends RuntimeException {

    public NoDriversAvailableException(String message) {
        super(message);
    }
}
