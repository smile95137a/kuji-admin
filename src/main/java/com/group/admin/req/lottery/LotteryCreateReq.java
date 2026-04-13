package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽獎商品建立請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "抽獎商品建立請求")
public class LotteryCreateReq {

    /**
     * 所屬店家ID
     * 
     * ⚠️ 不加 @NotBlank 驗證，因為：
     * - StoreOwner 可以不傳，後端自動帶入
     * - Admin 必須明確指定，由 Controller 驗證
     */
    @Schema(description = "所屬店家ID（StoreOwner 可不傳，後端自動帶入）", 
            example = "550e8400-e29b-41d4-a716-446655440000", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String storeId;

    /**
     * 商品/活動名稱
     */
    @NotBlank(message = "商品名稱不可為空")
    @Size(max = 255, message = "商品名稱最多255字")
    @Schema(description = "商品/活動名稱", example = "鬼滅之刃一番賞", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 詳細描述
     */
    @Schema(description = "商品詳細描述", example = "限量發售的鬼滅之刃一番賞，共有 A~G 獎項")
    private String description;

    /**
     * 商品主圖 URL
     */
    @Schema(description = "商品主圖 URL", example = "https://example.com/images/kimetsu.jpg")
    private String imageUrl;

    /**
     * 商品分類
     * OFFICIAL_ICHIBAN: 官方一番賞
     * GACHA: 扭蛋
     * TRADING_CARD: 卡牌
     * CUSTOM_GACHA: 自製賞
     */
    @NotBlank(message = "商品分類不可為空")
    @Schema(description = "商品分類：OFFICIAL_ICHIBAN/GACHA/TRADING_CARD/CUSTOM_GACHA", 
            example = "OFFICIAL_ICHIBAN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    /**
     * 自製賞子類型
     * LOTTERY_MODE: 抽籤型
     * SCRATCH_MODE: 刮刮樂型
     */
    @Schema(description = "自製賞子類型：LOTTERY_MODE/SCRATCH_MODE（僅 CUSTOM_GACHA 需要）", example = "LOTTERY_MODE")
    private String subCategory;

    /**
     * 每抽價格
     */
    @NotNull(message = "每抽價格不可為空")
    @Min(value = 0, message = "價格不可為負數")
    @Schema(description = "每抽價格", example = "650", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pricePerDraw;

    /**
     * 大獎售完後折扣價（若有設定）
     */
    @Schema(description = "大獎售完後的折扣價（留空=不折扣）", example = "500")
    private Long discountedPrice;

    /**
     * 是否啟用自動降價
     */
    @Schema(description = "是否啟用大獎售完後自動降價", example = "true")
    private Boolean autoDiscountEnabled;

    /**
     * 是否允許多抽
     */
    @Schema(description = "是否允許多抽", example = "true")
    private Boolean allowMultiDraw;

    /**
     * 多抽選項
     */
    @Schema(description = "多抽選項列表", example = "[10, 50]")
    private List<Integer> multiDrawOptions;

    /**
     * 定時上架時間
     */
    @Schema(description = "定時上架時間（留空=手動上架）", example = "2025-01-01T10:00:00")
    private LocalDateTime scheduledAt;

    /**
     * 活動開始時間
     */
    @Schema(description = "活動開始時間", example = "2025-01-01T10:00:00")
    private LocalDateTime startTime;

    /**
     * 活動結束時間
     */
    @Schema(description = "活動結束時間", example = "2025-12-31T23:59:59")
    private LocalDateTime endTime;

    /**
     * 總抽數上限（根據遊戲模式有不同規則）
     * 
     * 🆕 新架構（2025-12-25）：
     * 
     * <b>一番賞模式（LOTTERY_MODE）</b>：
     * - ❌ 不建議傳入此欄位，後端會自動計算 = 獎品總數
     * - 每個籤位都必須有獎品，不能有謝謝惠顧
     * - 例如：3 個獎品 → maxDraws 自動設為 3
     * 
     * <b>刮刮樂模式（SCRATCH_MODE）</b>：
     * - ✅ 必須傳入此欄位，支援謝謝惠顧
     * - maxDraws 必須 >= 獎品總數
     * - 剩餘的籤位會是謝謝惠顧
     * - 例如：2 個獎品 + 傳入 maxDraws=50 → 生成 2 個中獎籤位 + 48 個謝謝惠顧
     * 
     * <b>驗證規則</b>：
     * - 一番賞：maxDraws = 獎品總數（後端自動覆寫）
     * - 刮刮樂：maxDraws >= 獎品總數（使用前端設定值）
     */
    @Min(value = 0, message = "抽數上限不可為負數")
    @Schema(description = "總抽數上限（一番賞：自動計算；刮刮樂：必須傳入，支援謝謝惠顧）", 
            example = "80", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer maxDraws;

    /**
     * 顯示排序（數字越小越前面）
     */
    @Schema(description = "顯示排序", example = "1")
    private Integer orderNum;

    /**
     * 內部備註
     */
    @Schema(description = "內部備註（不對外顯示）", example = "這批貨進價較高")
    private String remark;

    /**
     * 遊玩模式
     */
    @Schema(description = "遊玩模式：LOTTERY_MODE（抽籤型）/ SCRATCH_MODE（刮刮樂型）", example = "LOTTERY_MODE")
    private String playMode;

    /**
     * 遊戲模式（刮刮樂子模式）
     * SCRATCH_STORE: 店家預先指定大獎位置
     * SCRATCH_PLAYER: 玩家開套時指定大獎位置
     * RANDOM: 完全隨機
     */
    @Schema(description = "遊戲模式：SCRATCH_STORE/SCRATCH_PLAYER/RANDOM（僅 SCRATCH_MODE 需要）", example = "RANDOM")
    @Pattern(regexp = "^(RANDOM|SCRATCH_STORE|SCRATCH_PLAYER|TICKET)?$", message = "gameMode 必須為 RANDOM、SCRATCH_STORE、SCRATCH_PLAYER 或 TICKET")
    private String gameMode;

    /**
     * 商品狀態
     */
    @Schema(description = "商品狀態：DRAFT（草稿）/ ON_SHELF（上架）/ OFF_SHELF（下架）", example = "DRAFT")
    private String status;

    /**
     * 熱門程度
     */
    @Schema(description = "熱門程度（用於顯示熱門標籤）", example = "999")
    private Integer hotCount;

    /**
     * 商品主題分類
     */
    @Schema(description = "商品主題分類（火影忍者、航海王、鬼滅之刃等）", example = "鬼滅之刃")
    private String theme;

    /**
     * 商品圖集
     */
    @Schema(description = "商品圖集（多張圖片URL）", example = "[\"https://example.com/images/1.jpg\", \"https://example.com/images/2.jpg\"]")
    private List<String> galleryImages;

    /**
     * 商品詳細內容
     */
    @Schema(description = "商品詳細內容（HTML格式）", example = "【活動說明】\\n- 單抽 / 多抽（10、50）\\n- 啟用自動折扣後，每抽 100 元")
    private String content;

    /**
     * 標籤列表
     */
    @Schema(description = "標籤列表", example = "[\"鬼滅之刃\", \"一番賞\", \"熱門\"]")
    private List<String> tags;

    /**
     * 是否啟用紅利點數
     */
    @Schema(description = "是否啟用紅利點數", example = "true")
    private Boolean bonusEnabled;

    /**
     * 每抽贈送紅利點數
     */
    @Schema(description = "每抽贈送紅利點數", example = "10")
    private Integer bonusPointsPerDraw;

    /**
     * 每抽消耗紅利點數
     */
    @Schema(description = "每抽消耗紅利點數", example = "200")
    private Integer bonusCostPerDraw;
}
