package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.Analysis;
import com.bin.dto.vo.AnalysisVO;
import lombok.NonNull;

import java.util.List;

/**
 * 实验分析service接口
 */
public interface AnalysisService extends IService<Analysis> {
    /**
     * 根据实验人姓名查询实验分析数据
     * @param name 实验人姓名
     * @return 实验分析数据
     */
    List<AnalysisVO> getByName(@NonNull String name);

    /**
     * 调用大模型分析数据，判断实验数据是否有不合理的地方
     * @param name 实验人姓名
     * @return 分析结果
     */
    String model(@NonNull String name);
}
