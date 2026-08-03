package com.pedrorios.make_shorty.controller;

import com.pedrorios.make_shorty.service.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
public class ShortUrlController {

    private final ShortUrlService service;

    public ShortUrlController(ShortUrlService service) {
        this.service = service;
    }

    @PostMapping("/shorten")
    public ResponseEntity<Map<String, String>> shorten(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "url is required"));
        }
        if (!url.matches("^https?://.+")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "invalid url"));
        }
        String code = service.shorten(url);
        return ResponseEntity.ok(Map.of("short", code));
    }

    @GetMapping("/{code}")
    public void redirect(@PathVariable String code, HttpServletResponse response)
            throws IOException {
        try {
            String target = service.resolve(code);
            response.sendRedirect(target);
        } catch (RuntimeException e) {
            response.sendError(404);
        }
    }
}