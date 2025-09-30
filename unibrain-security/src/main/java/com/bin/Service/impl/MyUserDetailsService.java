package com.bin.Service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bin.entity.MyUserDetails;
import com.bin.entity.User;
import com.bin.mapper.RolesMapper;
import com.bin.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RolesMapper rolesMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        // 判断user是否为空
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException(username);
        }
        if (!user.isStatus()) {
            throw new RuntimeException("用户已被禁用");
        }
        user.setRoles(rolesMapper.getRolesByUserId(user.getId()));
        return new MyUserDetails(user);
    }
}
