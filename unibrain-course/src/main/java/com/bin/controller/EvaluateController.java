package com.bin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.bin.dto.Evaluate;
import com.bin.dto.vo.EvaluatePageQuery;
import com.bin.dto.vo.EvaluateVO;
import com.bin.dto.vo.PageResult;
import com.bin.response.ApiResponse;
import com.bin.service.EvaluateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 评价课程Controller
 * @author bin
 */
@RestController
@RequestMapping("/api/evaluate")
public class EvaluateController {
    private static final Logger log = LoggerFactory.getLogger(EvaluateController.class);
    @Autowired
    private EvaluateService evaluateService;
    /**
     * 查询评价课程（用户名不能为空）
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getEvaluate")
    public ApiResponse<List<EvaluateVO>> getEvaluate(EvaluatePageQuery query) {
        log.info("查询评价课程，参数：{}", query);
        List<EvaluateVO> evaluateList = evaluateService.getEvaluate(query);
        return ApiResponse.success(evaluateList);
    }
    /**
     * 评价课程
     */
    @PreAuthorize("hasAnyRole('USER')")
    @PutMapping("/updateEvaluate")
    public ApiResponse<String> updateEvaluate(@RequestBody Evaluate evaluate) {
        if (evaluate.getId() == null) {
            throw new IllegalArgumentException("未选择评价课程");
        }
        evaluateService.updateEvaluate(evaluate);
        return ApiResponse.success();
    }
    /**
     * 根据评价id查询评价课程
     */
    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/getById")
    public ApiResponse<EvaluateVO> getById(@RequestParam("id") Long id) {
        EvaluateVO evaluateVO = evaluateService.getEvaluateById(id);
        return ApiResponse.success(evaluateVO);
    }
}
