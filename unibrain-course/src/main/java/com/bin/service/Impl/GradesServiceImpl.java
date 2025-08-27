package com.bin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Grades;
import com.bin.dto.vo.GradesVO;
import com.bin.mapper.GradesMapper;
import com.bin.service.GradesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成绩Service实现类
 * @author bin
 */
@Service
public class GradesServiceImpl extends ServiceImpl<GradesMapper, Grades> implements GradesService {

    @Autowired
    private GradesMapper gradesMapper;
    /**
     * 根据用户名查询成绩
     * @param userName 用户名
     * @return 成绩列表
     */
    @Override
    public List<GradesVO> getByUserName(String userName) {
        //判断用户名是否为空
        if (userName.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        //查询数据库中是否有该用户
        boolean exists = gradesMapper.exists(new LambdaQueryWrapper<Grades>().eq(Grades::getUserName, userName));
        if (!exists) {
            throw new IllegalArgumentException("用户不存在");
        }
        //查询该用户的成绩
        List<Grades> gradesList = gradesMapper.selectList(new LambdaQueryWrapper<Grades>().eq(Grades::getUserName, userName));
        //将查询结果转换为VO类并返回
        return BeanUtil.copyToList(gradesList, GradesVO.class);
    }
}
