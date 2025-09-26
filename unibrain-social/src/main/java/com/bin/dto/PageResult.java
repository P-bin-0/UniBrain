package com.bin.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    /**
     * 当前页数据列表
     */
    private List<T> records;

    /**
     * 总数据条数（仅传统分页需要；滚动加载可省略或返回null）
     */
    private Long total;

    /**
     * 总页数（total / pageSize，向上取整）
     */
    private Integer totalPage;

    /**
     * 当前页码
     */
    private Integer currentPage;

    /**
     * 每页条数
     */
    private Integer pageSize;


    // 构造方法：传统分页（含total）
    public PageResult(List<T> records, Long total, Integer currentPage, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPage = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
    }

}
