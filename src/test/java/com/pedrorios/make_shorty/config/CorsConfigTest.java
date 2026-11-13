package com.pedrorios.make_shorty.config;

import com.pedrorios.make_shorty.controller.ShortUrlController;
import com.pedrorios.make_shorty.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortUrlController.class)
class CorsConfigTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ShortUrlService service;

    @Test
    void allowsTheFrontendOrigin() throws Exception {
        mvc.perform(options("/shorten")
                        .header("Origin", "https://makeshortyweb.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://makeshortyweb.vercel.app"));
    }

    @Test
    void rejectsOtherOrigins() throws Exception {
        mvc.perform(options("/shorten")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
