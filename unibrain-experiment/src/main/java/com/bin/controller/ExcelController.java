package com.bin.controller;

import com.bin.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/excel")
public class ExcelController {
    @Autowired
    private ExcelService excelService;
    /**
     * 上传文件
     * @param file 文件
     * @return 上传结果
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            return excelService.importExcel(file);
        } catch (IOException e) {
            return "上传失败";
        }
    }
}
