package com.pedrorios.make_shorty.service;

import com.pedrorios.make_shorty.model.ShortUrl;
import com.pedrorios.make_shorty.repository.ShortUrlRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ShortUrlService {

    private final ShortUrlRepository repo;
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public ShortUrlService(ShortUrlRepository repo) {
        this.repo = repo;
    }
    @Transactional
    public String shorten(String url) {
        ShortUrl su = new ShortUrl();
        su.setOriginalUrl(url);
        su = repo.save(su); // insert → retorna a entidade com ID
        su.setCode(toBase62(su.getId()));
        repo.save(su); // update com o code
        return su.getCode();
    }

    public String resolve(String code) {
        return repo.findByCode(code)
                .map(ShortUrl::getOriginalUrl)
                .orElseThrow(() -> new RuntimeException("Link not found"));
    }

    private String toBase62(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(ALPHABET.charAt((int) (id % 62)));
            id /= 62;
        }
        return sb.reverse().toString();
    }
}