package com.bin.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.User;
import com.bin.mapper.UserMapper;
import com.bin.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
