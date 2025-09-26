package com.bin.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 通用分页查询参数（适用于所有分页接口）
 */
@Data
public class PageQueryDTO {
    /**
     * 页码：默认1（前端更友好，避免0起始的混淆）
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    /**
     * 每页条数：默认10，最大50（避免前端传过大的size导致性能问题）
     */
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 50, message = "每页条数不能超过50")
    private Integer pageSize = 10;

    /**
     * 排序字段：如 "createAt"（默认按创建时间排序）
     */
    private String sortField = "createAt";

    /**
     * 排序方向：ASC（升序）/ DESC（降序，默认）
     */
    private String sortDir = "DESC";

}
