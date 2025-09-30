package com.bin.controller;

import com.bin.Service.impl.UserLoginService;
import com.bin.entity.LoginUser;
import com.bin.entity.User;
import com.bin.response.ApiResponse;
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
    // 测试接口
    @GetMapping("/test")
    public ApiResponse<String> test() {
        return ApiResponse.success("测试成功");
    }
}
