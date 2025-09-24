package com.bin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Analysis;
import com.bin.dto.AnalysisDTO;
import com.bin.mapper.ExcelMapper;
import com.bin.service.ExcelService;
import com.bin.util.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件上传服务实现类
 */
@Service
public class ExcelServiceImpl extends ServiceImpl<ExcelMapper, Analysis> implements ExcelService {
    @Autowired
    private ExcelMapper excelMapper;

    /**
     * 导入Excel文件
     * @param file 文件
     * @throws IOException 异常
     */
    @Override
    public String importExcel(MultipartFile file) throws IOException {
        // 生成批次id
        String batchId = UUID.randomUUID().toString();
        //验证文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件为空");
        }
        //验证文件类型以及文件name
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new IllegalArgumentException("上传的文件类型必须为Excel文件");
        }
        //验证文件大小
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("上传的文件大小不能超过10MB");
        }
        //解析Excel文件
        List<AnalysisDTO> analysisDTOList = ExcelUtils.parseExcel(file);
        //将analysisList转换为Analysis
        List<Analysis> analysisList = analysisDTOList.stream()
                .map(dto -> {
                    Analysis analysis = convertToAnalysis(dto);
                    analysis.setBatchId(batchId);
                    return analysis;
                })
                .collect(Collectors.toList());
        //保存数据到数据库
        excelMapper.insert(analysisList);
        return batchId;
    }

    /**
     * 根据批次id查询数据
     * @param batchId 批次id
     * @return 数据列表
     */
    @Override
    public List<Analysis> getByBatchId(String batchId) {
        LambdaQueryWrapper<Analysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Analysis::getBatchId, batchId);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 将AnalysisDTO转换为Analysis对象
     * @param dto AnalysisDTO对象
     * @return Analysis对象
     */
    private Analysis convertToAnalysis(AnalysisDTO dto) {
        Analysis analysis = new Analysis();
        analysis.setName(dto.getName());
        analysis.setExDate(dto.getExDate());
        analysis.setExApparatus(dto.getExApparatus());
        analysis.setExEnvironment(dto.getExEnvironment());
        analysis.setLength(dto.getLength());

        // 将String类型的period转换为List<Double>
        if (dto.getPeriod() != null && !dto.getPeriod().trim().isEmpty()) {
            try {
                String periodStr = dto.getPeriod().trim();
                List<Double> periodList = new ArrayList<>();

                // 支持多种分隔符：逗号、空格、分号、竖线
                String[] separators = {",", " ", ";", "|"};
                String[] parts = null;

                for (String separator : separators) {
                    if (periodStr.contains(separator)) {
                        parts = periodStr.split(separator);
                        break;
                    }
                }

                // 如果没有找到分隔符，当作单个数值处理
                if (parts == null) {
                    parts = new String[]{periodStr};
                }

                for (String part : parts) {
                    part = part.trim();
                    if (!part.isEmpty()) {
                        try {
                            // 去除方括号等特殊字符
                            String cleanPart = part.replaceAll("[" + Pattern.quote("[]{}\"") + "]", "");
                            if (!cleanPart.isEmpty()) {
                                periodList.add(Double.parseDouble(cleanPart));
                            }
                        } catch (NumberFormatException e) {
                            // 跳过无法解析的数值
                            System.out.println("警告：无法解析周期数值: " + part);
                        }
                    }
                }

                // 如果解析后列表为空，设置默认值
                if (periodList.isEmpty()) {
                }

                analysis.setPeriod(periodList.isEmpty() ? null : periodList);
            } catch (Exception e) {
                // 解析失败，设置默认值
                analysis.setPeriod(null);
                System.out.println("警告：周期数据解析失败，设置为null: " + e.getMessage());
            }
        } else {
            // 空值处理，设置默认值
            analysis.setPeriod(null);
        }

        return analysis;
    }
}
