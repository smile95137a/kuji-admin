package com.group.admin.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 抽獎請求 DTO
 * 
 * @author KUJI Team
 * @since 2025-12-25
 */
@Data
public class DrawReq {
    
    /**
     * 抽獎次數（1-10 次）
     */
    @NotNull(message = "抽獎次數不能為空")
    @Min(value = 1, message = "抽獎次數最少 1 次")
    @Max(value = 10, message = "抽獎次數最多 10 次")
    private Integer count;
}
