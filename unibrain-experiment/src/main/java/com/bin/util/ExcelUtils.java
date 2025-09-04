package com.bin.util;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.bin.dto.Analysis;
import com.bin.dto.AnalysisDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel工具类
 */
public class ExcelUtils {
    /**
     * 解析Excel文件
     * @param file 文件
     * @return 数据列表
     */
    public static List<AnalysisDTO> parseExcel(MultipartFile file) {
        try {
            List<AnalysisDTO> analysisList = new ArrayList<AnalysisDTO>();
            //读取Excel文件
            EasyExcel.read(file.getInputStream(), AnalysisDTO.class, new AnalysisEventListener<AnalysisDTO>() {
                @Override
                public void invoke(AnalysisDTO analysisDTO, AnalysisContext analysisContext) {
                    analysisList.add(analysisDTO);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {

                }
            }).sheet().doRead();
            //转换为Analysis对象
            return analysisList.stream()
                    .map(ExcelUtils::convertToAnalysis)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("解析Excel文件失败", e);
        }
    }
    /**
     * 将Excel数据转换为Analysis对象
     * @param data Excel数据
     * @return Analysis对象
     */
    private static AnalysisDTO convertToAnalysis(AnalysisDTO data) {
        AnalysisDTO analysis = new AnalysisDTO();
        analysis.setName(data.getName());
        analysis.setExDate(data.getExDate());
        analysis.setExApparatus(data.getExApparatus());
        analysis.setExEnvironment(data.getExEnvironment());
        analysis.setLength(data.getLength());
        analysis.setPeriod(data.getPeriod());
        return analysis;
    }
}
