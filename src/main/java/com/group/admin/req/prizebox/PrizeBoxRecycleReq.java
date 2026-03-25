package com.group.admin.req.prizebox;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 回收獎品請求（轉換為紅利）
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
public class PrizeBoxRecycleReq {
    
    /**
     * 要回收的獎品盒項目 ID 列表
     */
    @NotEmpty(message = "請選擇要回收的獎品")
    private List<String> prizeBoxIds;
}
