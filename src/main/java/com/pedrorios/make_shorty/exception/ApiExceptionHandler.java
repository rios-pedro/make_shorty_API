package com.pedrorios.make_shorty.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(LinkNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", "link not found"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        // "url is required" tem prioridade: uma url vazia também falha no @Pattern
        String message = e.getBindingResult().getFieldErrors().stream()
                .filter(err -> "NotBlank".equals(err.getCode()))
                .findFirst()
                .or(() -> e.getBindingResult().getFieldErrors().stream().findFirst())
                .map(FieldError::getDefaultMessage)
                .orElse("invalid request");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid request body"));
    }
}
