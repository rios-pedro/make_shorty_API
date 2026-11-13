package com.pedrorios.make_shorty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShortenRequest(
        @NotBlank(message = "url is required")
        @Size(max = 2048, message = "url is too long")
        @Pattern(regexp = "^https?://\\S+$", message = "invalid url")
        String url
) {}
