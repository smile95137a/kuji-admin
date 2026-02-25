package com.group.admin.res.lottery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 籤位回應 DTO
 * 
 * <p>⚠️ 前台版本會隱藏未抽籤位的獎品資訊</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryTicketRes {
    
    /**
     * 籤位 ID
     */
    private String id;
    
    /**
     * 籤位編號 (1-N)
     */
    private Integer ticketNumber;

    /**
     * 刮刮樂專用：刮開後揭露的號碼（一番賞/扭蛋為 null）
     * 前台小：僅在 status=DRAWN 時才回傳
     */
    private Integer revealedNumber;

    /**
     * 狀態：AVAILABLE(可抽)/DRAWN(已抽)/LOCKED(鎖定中)
     */
    private String status;
    
    // ========== 以下欄位前台版本會根據狀態決定是否返回 ==========
    
    /**
     * 獎品 ID（前台：只有已抽才返回）
     */
    private String prizeId;
    
    /**
     * 獎品等級（前台：只有已抽才返回）
     * 例：A, B, C, LAST, THANKS
     */
    private String prizeLevel;
    
    /**
     * 獎品名稱（前台：只有已抽才返回）
     */
    private String prizeName;
    
    /**
     * 獎品圖片（前台：只有已抽才返回）
     */
    private String prizeImageUrl;
    
    /**
     * 是否為大獎（前台：只有已抽才返回）
     */
    private Boolean isGrandPrize;
    
    /**
     * 是否為最後賞（前台：只有已抽才返回）
     */
    private Boolean isLastPrize;
    
    // ========== 已抽相關資訊 ==========
    
    /**
     * 抽取者暱稱（前台顯示用）
     */
    private String drawnByNickname;
    
    /**
     * 抽取時間
     */
    private LocalDateTime drawnAt;
    
    // ========== 刮刮樂專用 ==========
    
    /**
     * 是否為指定大獎位置（後台用）
     */
    private Boolean isDesignatedPrize;
    
    /**
     * 建立前台安全版本（隱藏未抽籤位的獎品資訊）
     */
    public static LotteryTicketRes forFrontend(LotteryTicketRes full) {
        if (!"DRAWN".equals(full.getStatus())) {
            // 未抽：只返回基本資訊
            return LotteryTicketRes.builder()
                    .id(full.getId())
                    .ticketNumber(full.getTicketNumber())
                    .status(full.getStatus())
                    .build();
        }
        // 已抽：返回完整資訊
        return full;
    }
}
