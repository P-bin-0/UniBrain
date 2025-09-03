package com.bin.dto.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实验分析VO类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisVO {
    private Integer id; // 主键
    private String name; // 实验人姓名
    private LocalDateTime exDate; // 实验日期
    private String exApparatus; // 实验设备
    private Integer exEnvironment; // 实验环境（摄氏度）
    private Integer length; // 摆长（cm）
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Double> period; // 周期（秒）
}
