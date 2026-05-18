package com.example.demo.weather.exception;

public class UpstreamWeatherException extends RuntimeException {
    public UpstreamWeatherException(String message) {
        super(message);
    }

    public UpstreamWeatherException(String message, Throwable cause) {
        super(message, cause);
    }
}
