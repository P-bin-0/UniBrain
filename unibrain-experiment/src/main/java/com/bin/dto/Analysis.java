package com.bin.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实验分析实体类
 * @author bin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "analysis", autoResultMap = true)
public class Analysis {
    @TableId(type = IdType.AUTO)
    @ExcelProperty(value = "id")
    private Long id; // 主键
    @ExcelProperty(value = "name")
    private String name; // 实验人姓名
    @ExcelProperty(value = "ex_date")
    private LocalDateTime exDate; // 实验日期
    @ExcelProperty(value = "ex_apparatus")
    private String exApparatus; // 实验设备
    @ExcelProperty(value = "ex_environment")
    private Integer exEnvironment; // 实验环境（摄氏度）
    @ExcelProperty(value = "length")
    private Integer length; // 摆长（cm）
    @ExcelProperty(value = "period")
    // 存储JSON字符串，后端用List<Double>接收
    @TableField(typeHandler = JacksonTypeHandler.class) //MyBatis-Plus类型处理器
    private List<Double> period; // 周期（秒）
    @ExcelProperty(value = "batch_id")
    private String batchId; // 批次id
}
