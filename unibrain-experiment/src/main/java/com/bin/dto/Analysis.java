package com.bin.dto;

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
    private Long id; // 主键
    private String name; // 实验人姓名
    private LocalDateTime exDate; // 实验日期
    private String exApparatus; // 实验设备
    private Integer exEnvironment; // 实验环境（摄氏度）
    private Integer length; // 摆长（cm）
    // 存储JSON字符串，后端用List<Double>接收
    @TableField(typeHandler = JacksonTypeHandler.class) //MyBatis-Plus类型处理器
    private List<Double> period; // 周期（秒）
}
