package com.group.admin.res.draw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抽獎回應（包含錢包資訊）
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "抽獎完整回應")
public class DrawResponseRes {
    
    @Schema(description = "抽獎結果列表")
    private List<DrawResultRes> results;
    
    @Schema(description = "本次使用的 Gold")
    private Long goldUsed;
    
    @Schema(description = "本次使用的 Bonus")
    private Long bonusUsed;
    
    @Schema(description = "剩餘 Gold")
    private Long remainingGold;
    
    @Schema(description = "剩餘 Bonus")
    private Long remainingBonus;
    
    @Schema(description = "總抽獎次數")
    private Integer totalCount;
}
