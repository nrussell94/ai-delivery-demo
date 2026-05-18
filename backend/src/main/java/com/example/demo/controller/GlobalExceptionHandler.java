package com.example.demo.controller;

import com.example.demo.api.model.ErrorResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getField() + " " + err.getDefaultMessage())
            .orElse("Validation failed");
        return badRequest(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        JsonMappingException jme = findCause(ex, JsonMappingException.class);
        if (jme == null) {
            return badRequest("Request body is malformed");
        }
        String field = lastFieldName(jme);
        if (field == null) {
            return badRequest("Request body is malformed");
        }
        Class<?> target = targetType(jme);
        if (target != null && target.isEnum()) {
            return badRequest(humanize(field) + " must be one of: " + enumValues(target));
        }
        if (jme instanceof InvalidFormatException || jme instanceof ValueInstantiationException) {
            return badRequest(humanize(field) + " is in the wrong format");
        }
        if (jme instanceof MismatchedInputException) {
            return badRequest(humanize(field) + " is required");
        }
        return badRequest("Request body is malformed");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return badRequest(ex.getName() + " is invalid");
    }

    private static ResponseEntity<ErrorResponse> badRequest(String message) {
        ErrorResponse body = new ErrorResponse();
        body.setMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private static String lastFieldName(JsonMappingException ex) {
        List<JsonMappingException.Reference> path = ex.getPath();
        if (path == null || path.isEmpty()) {
            return null;
        }
        for (int i = path.size() - 1; i >= 0; i--) {
            String name = path.get(i).getFieldName();
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    private static Class<?> targetType(JsonMappingException jme) {
        if (jme instanceof InvalidFormatException ife) {
            return ife.getTargetType();
        }
        if (jme instanceof ValueInstantiationException vie) {
            JavaType jt = vie.getType();
            return jt != null ? jt.getRawClass() : null;
        }
        return null;
    }

    private static <T extends Throwable> T findCause(Throwable from, Class<T> type) {
        Throwable t = from;
        while (t != null) {
            if (type.isInstance(t)) {
                return type.cast(t);
            }
            Throwable next = t.getCause();
            if (next == t) {
                return null;
            }
            t = next;
        }
        return null;
    }

    private static String humanize(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        String spaced = camelCase.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private static String enumValues(Class<?> enumType) {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null) {
            return "";
        }
        return Arrays.stream(constants).map(Object::toString).collect(Collectors.joining(", "));
    }
}
