package com.iboot.iprint.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iboot.iprint.entity.PrintTemplate;

import java.util.List;
import java.util.Optional;

public interface PrintTemplateRepository extends BaseMapper<PrintTemplate> {

    default Optional<PrintTemplate> findById(Long id) {
        return Optional.ofNullable(selectById(id));
    }

    default List<PrintTemplate> findAll() {
        return selectList(null);
    }

    default Optional<PrintTemplate> findByCode(String code) {
        return Optional.ofNullable(selectOne(Wrappers.<PrintTemplate>lambdaQuery()
                .eq(PrintTemplate::getCode, code)));
    }

    default boolean existsByCode(String code) {
        return selectCount(Wrappers.<PrintTemplate>lambdaQuery()
                .eq(PrintTemplate::getCode, code)) > 0;
    }

    default long count() {
        return selectCount(null);
    }

    default void save(PrintTemplate entity) {
        if (entity.getId() == null) {
            insert(entity);
        } else {
            updateById(entity);
        }
    }

    default void delete(PrintTemplate entity) {
        if (entity != null && entity.getId() != null) {
            deleteById(entity.getId());
        }
    }

    default IPage<PrintTemplate> findByKeyword(String keyword, Page<PrintTemplate> page) {
        return selectPage(page, Wrappers.<PrintTemplate>lambdaQuery()
                .and(keyword != null && !keyword.isBlank(), w ->
                        w.like(PrintTemplate::getCode, keyword).or().like(PrintTemplate::getName, keyword))
                .orderByDesc(PrintTemplate::getCreatedAt));
    }
}
