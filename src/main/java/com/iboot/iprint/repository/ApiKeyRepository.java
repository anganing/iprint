package com.iboot.iprint.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iboot.iprint.entity.ApiKey;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends BaseMapper<ApiKey> {

    default Optional<ApiKey> findById(Long id) {
        return Optional.ofNullable(selectById(id));
    }

    default List<ApiKey> findAll() {
        return selectList(null);
    }

    default void save(ApiKey entity) {
        if (entity.getId() == null) {
            insert(entity);
        } else {
            updateById(entity);
        }
    }

    default void delete(ApiKey entity) {
        if (entity != null && entity.getId() != null) {
            deleteById(entity.getId());
        }
    }

    default long count() {
        return selectCount(null);
    }

    default Optional<ApiKey> findByApiKey(String apiKey) {
        return Optional.ofNullable(selectOne(Wrappers.<ApiKey>lambdaQuery()
                .eq(ApiKey::getApiKey, apiKey)));
    }

    default boolean existsByApiKey(String apiKey) {
        return selectCount(Wrappers.<ApiKey>lambdaQuery()
                .eq(ApiKey::getApiKey, apiKey)) > 0;
    }

    default long countByStatus(Integer status) {
        return selectCount(Wrappers.<ApiKey>lambdaQuery()
                .eq(ApiKey::getStatus, status));
    }

    default IPage<ApiKey> findByKeywordAndStatus(String keyword, Integer status, Page<ApiKey> page) {
        return selectPage(page, Wrappers.<ApiKey>lambdaQuery()
                .and(keyword != null && !keyword.isBlank(), w ->
                        w.like(ApiKey::getName, keyword).or().like(ApiKey::getDescription, keyword))
                .and(status != null, w -> w.eq(ApiKey::getStatus, status))
                .orderByDesc(ApiKey::getCreatedAt));
    }
}
