package com.bin.controller;

import com.bin.Service.impl.UserLoginService;
import com.bin.entity.LoginUser;
import com.bin.entity.User;
import com.bin.response.ApiResponse;
import com.bin.util.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserLoginService userLoginService;

    /**
     * 用户登录
     */
    @PermitAll
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginUser loginUser) {
        return userLoginService.login(loginUser);
    }
    /**
     * 退出登录
     */
    @PermitAll
    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        return userLoginService.logout();
    }
    /**
     * 注册用户
     */
    @PermitAll
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody LoginUser loginUser) {
        System.out.println(loginUser.getUsername() + " " + loginUser.getPassword());
        if (loginUser.getPassword() == null || loginUser.getPassword().trim().isEmpty()) {
            return ApiResponse.error(400, "密码不能为空");
        }
        Boolean success = userLoginService.addUser(loginUser);
        if (success) {
            return ApiResponse.success("用户注册成功");
        } else {
            return ApiResponse.error(400, "用户注册失败");
        }
    }

    /**
     * 获取登录用户的信息
     */
    @PermitAll
    @GetMapping("/info")
    public ApiResponse<User> getUserInfo() {
        // 从 SecurityUtil 获取当前登录用户的 ID
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "用户未登录");
        }
        // 从数据库查询用户信息
        User user = userLoginService.selectById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        return ApiResponse.success(user);
    }
    // 测试接口
    @GetMapping("/test")
    public ApiResponse<String> test() {
        return ApiResponse.success("测试成功");
    }
}
