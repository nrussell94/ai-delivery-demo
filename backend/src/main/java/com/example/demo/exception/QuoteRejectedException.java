package com.example.demo.exception;

public class QuoteRejectedException extends RuntimeException {

    public QuoteRejectedException(String message) {
        super(message);
    }
}
