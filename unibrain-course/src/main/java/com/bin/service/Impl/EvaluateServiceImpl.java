package com.bin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.Service.impl.UserLoginService;
import com.bin.dto.Evaluate;
import com.bin.dto.vo.EvaluatePageQuery;
import com.bin.dto.vo.EvaluateVO;
import com.bin.dto.vo.PageResult;
import com.bin.mapper.EvaluateMapper;
import com.bin.service.EvaluateService;
import com.bin.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价课程Service实现类
 * @author bin
 */
@Service
public class EvaluateServiceImpl extends ServiceImpl<EvaluateMapper, Evaluate> implements EvaluateService {
    @Autowired
    private EvaluateMapper evaluateMapper;
    @Autowired
    private UserLoginService userLoginService;

    /**
     * 查询评价课程
     * @param query
     * @return
     */
    @Override
    public List<EvaluateVO> getEvaluate(EvaluatePageQuery query) {
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        // 根据ID获取当前登录用户的昵称
        String userName = userLoginService.selectById(userId).getUsername();
        //直接将数据库的数据查询出来
        LambdaQueryWrapper<Evaluate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluate::getUserName, userName);
        if (query.getSemester() != null) {
            wrapper.eq(Evaluate::getSemester, query.getSemester());
        }
        if (query.getIsEvaluate() != null) {
            wrapper.eq(Evaluate::getIsEvaluate, query.getIsEvaluate());
        }
        List<Evaluate> list = evaluateMapper.selectList(wrapper);

        //将分页结果转为VO对象
        List<EvaluateVO> resList = list.stream().map(evaluate -> {
            EvaluateVO evaluateVO = new EvaluateVO();
            BeanUtil.copyProperties(evaluate, evaluateVO);
            return evaluateVO;
        }).toList();
        //返回结果
        return resList;
    }

    /**
     * 评价课程
     */
    @Override
    public void updateEvaluate(Evaluate evaluate) {
        //判断评价课程是否存在
        if (!evaluateMapper.exists(new LambdaQueryWrapper<Evaluate>().eq(Evaluate::getId, evaluate.getId()))) {
            throw new IllegalArgumentException("评价课程不存在");
        }
        //构建更新条件
        LambdaUpdateWrapper<Evaluate> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Evaluate::getId, evaluate.getId());
        if (evaluate.getScore() != null) {
            wrapper.set(Evaluate::getScore, evaluate.getScore());
        }
        if (evaluate.getContext() != null) {
            wrapper.set(Evaluate::getContext, evaluate.getContext());
        }
        //更新
        int update = evaluateMapper.update(wrapper);
        if (update != 1) {
            throw new IllegalArgumentException("评价课程更新失败");
        }
        //更新评价状态
        wrapper.set(Evaluate::getIsEvaluate, true);
        update = evaluateMapper.update(wrapper);
        if (update != 1) {
            throw new IllegalArgumentException("评价课程更新失败");
        }
    }

    /**
     * 根据评价id查询评价课程
     */
    @Override
    public EvaluateVO getEvaluateById(Long id) {
        Evaluate evaluate = evaluateMapper.selectById(id);
        if (evaluate == null) {
            throw new IllegalArgumentException("评价课程不存在");
        }
        EvaluateVO evaluateVO = new EvaluateVO();
        BeanUtil.copyProperties(evaluate, evaluateVO);
        return evaluateVO;
    }
}
