package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.Analysis;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传
 */
public interface ExcelService extends IService<Analysis> {
    /**
     * 导入Excel文件
     * @param file 文件
     * @throws IOException 异常
     */
    void importExcel(MultipartFile file) throws IOException;
}
