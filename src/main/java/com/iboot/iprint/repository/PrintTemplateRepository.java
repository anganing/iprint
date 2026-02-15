package com.iboot.iprint.repository;

import com.iboot.iprint.entity.PrintTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrintTemplateRepository extends JpaRepository<PrintTemplate, Long> {
    Optional<PrintTemplate> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT t FROM PrintTemplate t WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(t.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<PrintTemplate> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
