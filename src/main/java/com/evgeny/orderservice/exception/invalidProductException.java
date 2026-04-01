package com.evgeny.orderservice.exception;

public class invalidProductException extends RuntimeException {
    public invalidProductException(String message) {
        super(message);
    }
}
