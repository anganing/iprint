package com.iboot.iprint.repository;

import com.iboot.iprint.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByApiKey(String apiKey);

    boolean existsByApiKey(String apiKey);
}
