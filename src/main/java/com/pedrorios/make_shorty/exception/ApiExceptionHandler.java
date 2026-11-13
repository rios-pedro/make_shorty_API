package com.pedrorios.make_shorty.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

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

    // Sem este handler, um erro inesperado sobe até o container e a resposta 500
    // sai sem os headers de CORS: o navegador esconde o status e o front acha que
    // é falha de rede. Tratando aqui, a resposta passa pelo Spring MVC e mantém o CORS.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
        // Exceções do próprio Spring (405, 415, rota inexistente...) já sabem o status certo.
        if (e instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            String reason = status instanceof HttpStatus hs ? hs.getReasonPhrase().toLowerCase() : "request error";
            return ResponseEntity.status(status).body(Map.of("error", reason));
        }
        log.error("Unexpected error", e);
        return ResponseEntity.internalServerError().body(Map.of("error", "internal error"));
    }
}
