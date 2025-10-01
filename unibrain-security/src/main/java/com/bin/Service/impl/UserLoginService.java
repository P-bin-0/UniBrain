package com.bin.Service.impl;

import com.bin.entity.LoginUser;
import com.bin.entity.MyUserDetails;
import com.bin.entity.User;
import com.bin.entity.UserRoles;
import com.bin.mapper.UserMapper;
import com.bin.mapper.UserRolesMapper;
import com.bin.response.ApiResponse;
import com.bin.util.JWTUtil;
import com.bin.util.RedisKeyUtil;
import com.bin.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class UserLoginService {

    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRolesMapper userRolesMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    public ApiResponse<String> login(LoginUser loginUser) {
        if (loginUser.getUsername() == null || loginUser.getUsername().isEmpty()) {
            return ApiResponse.error(400, "用户名为空");
        }
        if (loginUser.getPassword() == null || loginUser.getPassword().isEmpty()) {
            return ApiResponse.error(400, "密码不能为空");
        }
        String token;
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword()));
            if (Objects.isNull(authentication)) {
                throw new RuntimeException("认证失败");
            }
            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal(); // 获取用户详情
            Long id = userDetails.getUser().getId();
            System.out.println("用户ID：" + id);
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", id);
            token = jwtUtil.generateToken(userDetails.getUsername(), claims);
            String key = RedisKeyUtil.keyLoginUser(id);
            redisUtil.setCacheObject(key, userDetails);
        } catch (Exception e) {
            System.out.println("认证异常类型：" + e.getClass().getName());
            System.out.println("异常消息：" + e.getMessage());
            return ApiResponse.error(401, "用户名或密码错误");
        }

        return ApiResponse.success(token);
    }
    /**
     * 退出登录
     */
    public ApiResponse<String> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return ApiResponse.error(401, "未获取到当前登录用户");
        }
        Long id = ((MyUserDetails) authentication.getPrincipal()).getUser().getId();
        String key = RedisKeyUtil.keyLoginUser(id);
        redisUtil.delete(key);
        SecurityContextHolder.clearContext();
        return ApiResponse.success();
    }
    public Boolean addUser(LoginUser loginUser) {
        try {
            User user = new User();
            user.setUsername(loginUser.getUsername());
            user.setPassword(new BCryptPasswordEncoder().encode(loginUser.getPassword()));
            user.setStatus(true);
            int insert = userMapper.insert(user);
            if (insert > 0) {
                // 向角色表插入记录
                UserRoles userRoles = new UserRoles();
                userRoles.setUserId(user.getId());
                userRoles.setRoleId(3L); // 假设默认角色为3
                int row = userRolesMapper.insert(userRoles);
                return row > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public User selectById(Long userId) {
        return userMapper.selectById(userId);
    }
}
