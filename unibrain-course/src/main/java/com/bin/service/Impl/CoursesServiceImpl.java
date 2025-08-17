package com.bin.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Courses;
import com.bin.mapper.CoursesMapper;
import com.bin.service.CoursesService;

import org.springframework.stereotype.Service;

@Service
public class CoursesServiceImpl extends ServiceImpl<CoursesMapper, Courses> implements CoursesService {
}
