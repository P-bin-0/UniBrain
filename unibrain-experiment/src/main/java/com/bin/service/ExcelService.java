package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.Analysis;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件上传
 */
public interface ExcelService extends IService<Analysis> {
    /**
     * 导入Excel文件
     * @param file 文件
     * @throws IOException 异常
     */
    String importExcel(MultipartFile file) throws IOException;
    /**
     * 根据批次id查询数据
     */
    List<Analysis> getByBatchId(String batchId);
}
