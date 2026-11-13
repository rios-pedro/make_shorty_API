package com.pedrorios.make_shorty.service;

import com.pedrorios.make_shorty.exception.LinkNotFoundException;
import com.pedrorios.make_shorty.model.ShortUrl;
import com.pedrorios.make_shorty.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class ShortUrlService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7; // 62^7 ≈ 3,5 trilhões de combinações
    private static final int MAX_ATTEMPTS = 5;

    private final ShortUrlRepository repo;
    private final SecureRandom random = new SecureRandom();

    public ShortUrlService(ShortUrlRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public String shorten(String url) {
        String code = generateUniqueCode();
        ShortUrl su = new ShortUrl();
        su.setCode(code);
        su.setOriginalUrl(url);
        repo.save(su); // um único insert, já com o code preenchido
        return code;
    }

    @Transactional(readOnly = true)
    public String resolve(String code) {
        return repo.findByCode(code)
                .map(ShortUrl::getOriginalUrl)
                .orElseThrow(() -> new LinkNotFoundException(code));
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = randomCode();
            if (!repo.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate a unique code");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
