package com.pedrorios.make_shorty.service;

import com.pedrorios.make_shorty.exception.LinkNotFoundException;
import com.pedrorios.make_shorty.model.ShortUrl;
import com.pedrorios.make_shorty.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ShortUrlServiceTest {

    private final ShortUrlRepository repo = mock(ShortUrlRepository.class);
    private final ShortUrlService service = new ShortUrlService(repo);

    @Test
    void shortenSavesOnceWithCodeAlreadySet() {
        when(repo.existsByCode(anyString())).thenReturn(false);

        String code = service.shorten("https://example.com");

        ArgumentCaptor<ShortUrl> captor = ArgumentCaptor.forClass(ShortUrl.class);
        verify(repo, times(1)).save(captor.capture());
        assertEquals(code, captor.getValue().getCode());
        assertEquals("https://example.com", captor.getValue().getOriginalUrl());
        assertTrue(code.matches("[a-zA-Z0-9]{7}"), "code: " + code);
    }

    @Test
    void shortenRetriesOnCollision() {
        when(repo.existsByCode(anyString())).thenReturn(true, true, false);

        service.shorten("https://example.com");

        verify(repo, times(3)).existsByCode(anyString());
        verify(repo, times(1)).save(any());
    }

    @Test
    void shortenGivesUpAfterTooManyCollisions() {
        when(repo.existsByCode(anyString())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.shorten("https://example.com"));
        verify(repo, never()).save(any());
    }

    @Test
    void resolveReturnsOriginalUrl() {
        ShortUrl su = new ShortUrl();
        su.setCode("abc1234");
        su.setOriginalUrl("https://example.com");
        when(repo.findByCode("abc1234")).thenReturn(Optional.of(su));

        assertEquals("https://example.com", service.resolve("abc1234"));
    }

    @Test
    void resolveUnknownCodeThrowsNotFound() {
        when(repo.findByCode("nope")).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () -> service.resolve("nope"));
    }
}
