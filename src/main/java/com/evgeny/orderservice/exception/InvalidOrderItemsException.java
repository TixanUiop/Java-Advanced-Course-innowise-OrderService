package com.evgeny.orderservice.exception;

public class InvalidOrderItemsException extends RuntimeException {
    public InvalidOrderItemsException(String message) {
        super(message);
    }
}
