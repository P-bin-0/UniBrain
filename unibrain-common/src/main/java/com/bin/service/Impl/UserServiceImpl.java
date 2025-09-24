package com.bin.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.mapper.UserMapper;
import com.bin.model.entity.User;
import com.bin.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
