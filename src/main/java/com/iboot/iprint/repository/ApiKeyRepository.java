package com.iboot.iprint.repository;

import com.iboot.iprint.entity.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByApiKey(String apiKey);

    boolean existsByApiKey(String apiKey);

    long countByStatus(Integer status);

    @Query("SELECT a FROM ApiKey a WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "ORDER BY a.createdAt DESC")
    Page<ApiKey> findByKeywordAndStatus(@Param("keyword") String keyword,
                                        @Param("status") Integer status,
                                        Pageable pageable);
}
