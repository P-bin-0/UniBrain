package com.bin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Analysis;
import com.bin.dto.vo.AnalysisVO;
import com.bin.mapper.AnalysisMapper;
import com.bin.service.AnalysisService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实验分析service实现类
 */
@Service
public class AnalysisServiceImpl extends ServiceImpl<AnalysisMapper, Analysis> implements AnalysisService {
    /**
     * 根据实验人姓名查询实验分析数据
     * @param name 实验人姓名
     * @return 实验分析数据
     */
    @Override
    public List<AnalysisVO> getByName(@NonNull String name) {
        LambdaQueryWrapper<Analysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Analysis::getName, name);
        List<Analysis> analysisList = baseMapper.selectList(wrapper);
        if (analysisList.isEmpty()) {
            throw new RuntimeException("未找到该实验人");
        }
        // 转换为VO
        return analysisList.stream().map(analysis -> new AnalysisVO(
                analysis.getId().intValue(),
                analysis.getName(),
                analysis.getExDate(),
                analysis.getExApparatus(),
                analysis.getExEnvironment(),
                analysis.getLength(),
                analysis.getPeriod()
        )).collect(Collectors.toList());
    }
}
