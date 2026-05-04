package com.taxi.tripservice.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidTripOperationException extends RuntimeException {

    public InvalidTripOperationException(String message) {
        super(message);
    }
}
