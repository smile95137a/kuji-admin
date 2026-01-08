package com.group.admin.res.prizebox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 賞品盒摘要（按店家分組）
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrizeBoxSummaryRes {
    
    /**
     * 店家 ID
     */
    private String storeId;
    
    /**
     * 店家名稱
     */
    private String storeName;
    
    /**
     * 該店家的獎品數量
     */
    private Integer itemCount;
    
    /**
     * 該店家的獎品列表
     */
    private List<PrizeBoxItemRes> items;
}
