package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.Evaluate;
import com.bin.dto.vo.EvaluatePageQuery;
import com.bin.dto.vo.EvaluateVO;
import com.bin.dto.vo.PageResult;

import java.util.List;

/**
 * 评价课程Service
 * @author bin
 */
public interface EvaluateService extends IService<Evaluate> {
    /**
     * 查询评价课程
     */
    PageResult<EvaluateVO> getEvaluate(EvaluatePageQuery query);

    /**
     * 评价课程
     */
    void updateEvaluate(Evaluate evaluate);
}
