package com.iboot.iprint.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iboot.iprint.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseMapper<User> {

    default Optional<User> findById(Long id) {
        return Optional.ofNullable(selectById(id));
    }

    default List<User> findAll() {
        return selectList(null);
    }

    default User save(User entity) {
        if (entity.getId() == null) {
            insert(entity);
        } else {
            updateById(entity);
        }
        return entity;
    }

    default void delete(User entity) {
        if (entity != null && entity.getId() != null) {
            deleteById(entity.getId());
        }
    }

    default long count() {
        return selectCount(null);
    }

    default Optional<User> findByUsername(String username) {
        return Optional.ofNullable(selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username)));
    }

    default boolean existsByUsername(String username) {
        return selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username)) > 0;
    }

    default IPage<User> findByKeyword(String keyword, Page<User> page) {
        return selectPage(page, Wrappers.<User>lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), User::getUsername, keyword)
                .orderByDesc(User::getCreatedAt));
    }
}
