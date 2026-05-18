package com.example.demo.config;

import com.example.demo.weather.exception.LocationOutsideUkException;
import com.example.demo.weather.exception.UpstreamWeatherException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private record MessageResponse(String message) {}

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponse> handleValidation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(message));
    }

    @ExceptionHandler(LocationOutsideUkException.class)
    public ResponseEntity<MessageResponse> handleOutsideUk(LocationOutsideUkException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new MessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(UpstreamWeatherException.class)
    public ResponseEntity<MessageResponse> handleUpstream(UpstreamWeatherException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new MessageResponse(ex.getMessage()));
    }
}
