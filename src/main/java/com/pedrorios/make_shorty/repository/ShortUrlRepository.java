package com.pedrorios.make_shorty.repository;

import com.pedrorios.make_shorty.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByCode(String code);
}