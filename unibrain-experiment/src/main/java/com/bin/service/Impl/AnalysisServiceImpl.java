package com.bin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Analysis;
import com.bin.dto.vo.AnalysisVO;
import com.bin.mapper.AnalysisMapper;
import com.bin.service.AnalysisService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实验分析service实现类
 */
@Service
public class AnalysisServiceImpl extends ServiceImpl<AnalysisMapper, Analysis> implements AnalysisService {

    private ChatLanguageModel chatLanguageModel;

    public AnalysisServiceImpl(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }
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

    /**
     * 调用大模型分析数据，判断实验数据是否有不合理的地方
     * @param name 实验人姓名
     * @return 分析结果
     */
    @Override
    public String model(@NonNull String name) {
        //先根据姓名查询实验分析数据
        List<AnalysisVO> analysisVOList = getByName(name);
        // 将数据转换为字符串形式
        StringBuilder dataStr = new StringBuilder();
        for (AnalysisVO analysisVO : analysisVOList) {
            dataStr.append("姓名：").append(analysisVO.getName())
                    .append("，实验日期：").append(analysisVO.getExDate())
                    .append("，实验设备：").append(analysisVO.getExApparatus())
                    .append("，实验环境：").append(analysisVO.getExEnvironment())
                    .append("，摆长：").append(analysisVO.getLength())
                    .append("，周期：").append(analysisVO.getPeriod())
                    .append("\n");
        }
        // 构建提示词
        String prompt = """
                请对以下内容进行分析，判断是否存在异常数据。
                若有，请指出具体是哪一条（需说明对应行的姓名、
                实验时间等关键信息），并结合实验背景
                （实验仪器、实验环境等信息可从数据中获取），
                给出可能导致该异常的原因；若没有异常数据，
                请给出合理性建议使实验数据更加准确。
                实验数据：%s
                """.formatted(dataStr.toString());
        //调用大模型
        try {
            return chatLanguageModel.chat(prompt);
        } catch (Exception e) {
            throw new RuntimeException("调用大模型分析数据失败：" + e.getMessage(), e);
        }
    }
}
