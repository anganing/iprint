package com.iboot.iprint.repository;

import com.iboot.iprint.entity.PrintTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrintTemplateRepository extends JpaRepository<PrintTemplate, Long> {
    Optional<PrintTemplate> findByCode(String code);

    boolean existsByCode(String code);
}
