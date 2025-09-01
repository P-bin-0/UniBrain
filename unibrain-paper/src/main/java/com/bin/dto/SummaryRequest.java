package com.bin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 摘要请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryRequest {
    private String text;
}
