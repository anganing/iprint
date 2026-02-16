package com.iboot.iprint.controller;

import com.iboot.iprint.model.request.UserRequest;
import com.iboot.iprint.model.response.PageResult;
import com.iboot.iprint.model.response.UserInfoResponse;
import com.iboot.iprint.result.ApiResult;
import com.iboot.iprint.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResult<PageResult<UserInfoResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResult.ok(userService.listPage(keyword, page, size));
    }

    @PostMapping
    public ApiResult<UserInfoResponse> create(@RequestBody @Valid UserRequest request) {
        return ApiResult.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResult<UserInfoResponse> update(@PathVariable Long id,
                                              @RequestBody @Valid UserRequest request) {
        return ApiResult.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResult.ok();
    }
}
