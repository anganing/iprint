package com.iboot.iprint.service;

import com.iboot.iprint.exception.BusinessException;
import com.iboot.iprint.model.request.ChangePasswordRequest;
import com.iboot.iprint.model.request.UserRequest;
import com.iboot.iprint.model.response.PageResult;
import com.iboot.iprint.model.response.UserInfoResponse;
import com.iboot.iprint.entity.User;
import com.iboot.iprint.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("用户 {} 修改密码成功", username);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    public PageResult<UserInfoResponse> listPage(String keyword, int page, int size) {
        Page<User> pageData = userRepository.findByKeyword(keyword, PageRequest.of(page - 1, size));
        List<UserInfoResponse> content = pageData.getContent().stream()
                .map(u -> UserInfoResponse.builder().id(u.getId()).username(u.getUsername()).createdAt(u.getCreatedAt()).build())
                .collect(Collectors.toList());
        return PageResult.of(content, pageData.getTotalElements(), pageData.getTotalPages(), page, size);
    }

    @Transactional
    public UserInfoResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在: " + request.getUsername());
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);
        log.info("用户已创建: {}", user.getUsername());
        return UserInfoResponse.builder().id(user.getId()).username(user.getUsername()).createdAt(user.getCreatedAt()).build();
    }

    @Transactional
    public UserInfoResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在: " + request.getUsername());
        }
        user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        log.info("用户已更新: id={}", id);
        return UserInfoResponse.builder().id(user.getId()).username(user.getUsername()).createdAt(user.getCreatedAt()).build();
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        userRepository.delete(user);
        log.info("用户已删除: id={}, username={}", id, user.getUsername());
    }
}
