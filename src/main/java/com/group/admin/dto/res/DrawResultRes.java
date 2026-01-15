package com.group.admin.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 單次抽獎結果 DTO
 * 
 * @author KUJI Team
 * @since 2025-12-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawResultRes {
    
    /**
     * 賞品盒 ID（用於後續兌換/回收）
     */
    private String prizeBoxId;
    
    /**
     * 獎品 ID
     */
    private String prizeId;
    
    /**
     * 獎品名稱（例：魯夫 手辦）
     */
    private String prizeName;
    
    /**
     * 獎品等級（例：A賞、B賞、C賞）
     */
    private String prizeLevel;
    
    /**
     * 獎品圖片 URL
     */
    private String prizeImageUrl;
    
    /**
     * 獎品類型（PHYSICAL/POINT）
     */
    private String prizeType;
    
    /**
     * 點數價值（如果是 POINT 類型）
     */
    private Long pointValue;
    
    /**
     * 是否為賞終（Last Prize）
     */
    private Boolean isLastPrize;
    
    /**
     * 是否為大賞（Grand Prize）
     */
    private Boolean isGrandPrize;
    
    /**
     * 是否可回收
     */
    private Boolean isRecyclable;
    
    /**
     * 回收獎勵點數（Bonus）
     */
    private Long recycleBonus;
}
