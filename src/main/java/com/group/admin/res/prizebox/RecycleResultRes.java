package com.group.admin.res.prizebox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回收結果回應
 * 
 * @author Kuji Admin
 * @since 2026-03-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleResultRes {
    
    /**
     * 回收獲得的總紅利
     */
    private Long totalBonus;
    
    /**
     * 回收的獎品數量
     */
    private Integer recycledCount;
}
