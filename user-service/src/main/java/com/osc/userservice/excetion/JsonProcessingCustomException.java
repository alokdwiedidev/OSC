package com.osc.userservice.excetion;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonProcessingCustomException extends Throwable {

    public JsonProcessingCustomException(String message) {
        super(message);
    }
}
