package com.example.demo.weather.exception;

public class LocationOutsideUkException extends RuntimeException {
    public LocationOutsideUkException(String message) {
        super(message);
    }
}
