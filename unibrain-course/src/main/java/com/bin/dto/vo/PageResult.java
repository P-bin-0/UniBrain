package com.bin.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private Long total; //总记录数
    private Long pageNum; //当前页码
    private Long pageSize; //每页记录数
    private List<?> list;
}
