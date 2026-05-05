package com.evgeny.orderservice.exception;

public class InvalidOrExpiredToken extends RuntimeException {
    public InvalidOrExpiredToken() {
        super("Invalid or expired token");
    }
}
