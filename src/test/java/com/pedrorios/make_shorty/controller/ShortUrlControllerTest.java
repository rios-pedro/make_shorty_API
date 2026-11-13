package com.pedrorios.make_shorty.controller;

import com.pedrorios.make_shorty.exception.LinkNotFoundException;
import com.pedrorios.make_shorty.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortUrlController.class)
class ShortUrlControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ShortUrlService service;

    @Test
    void shortenReturnsCode() throws Exception {
        when(service.shorten("https://example.com")).thenReturn("aB3xY9z");

        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.short").value("aB3xY9z"));
    }

    @Test
    void shortenRejectsMissingUrl() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("url is required"));
    }

    @Test
    void shortenRejectsBlankUrl() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("url is required"));
    }

    @Test
    void shortenRejectsInvalidUrl() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid url"));
        verify(service, never()).shorten(anyString());
    }

    @Test
    void shortenRejectsTooLongUrl() throws Exception {
        String url = "https://example.com/" + "a".repeat(2100);
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + url + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("url is too long"));
    }

    @Test
    void shortenRejectsMalformedBody() throws Exception {
        mvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid request body"));
    }

    @Test
    void redirectSendsFoundWithLocation() throws Exception {
        when(service.resolve("aB3xY9z")).thenReturn("https://example.com");

        mvc.perform(get("/aB3xY9z"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void redirectUnknownCodeReturns404() throws Exception {
        when(service.resolve("nope")).thenThrow(new LinkNotFoundException("nope"));

        mvc.perform(get("/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("link not found"));
    }

    @Test
    void redirectDatabaseErrorIsNot404() throws Exception {
        when(service.resolve("abc")).thenThrow(new IllegalStateException("db down"));

        // Sem handler para essa exceção, ela sobe (no servidor real vira 500), em vez de virar 404
        jakarta.servlet.ServletException ex = org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class, () -> mvc.perform(get("/abc")));
        org.junit.jupiter.api.Assertions.assertInstanceOf(IllegalStateException.class, ex.getCause());
    }
}
