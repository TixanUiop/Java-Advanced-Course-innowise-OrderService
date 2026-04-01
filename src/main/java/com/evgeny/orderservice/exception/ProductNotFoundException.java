package com.evgeny.orderservice.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product not found with id " + productId);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
