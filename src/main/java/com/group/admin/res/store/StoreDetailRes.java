package com.group.admin.res.store;

import com.group.admin.res.lottery.LotteryListItemRes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公開店家詳情（含上架商品）")
public class StoreDetailRes {

    @Schema(description = "店家 ID")
    private String id;

    @Schema(description = "店家名稱")
    private String storeName;

    @Schema(description = "短描述")
    private String shortDescription;

    @Schema(description = "詳細介紹")
    private String longDescription;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "封面圖片 URL")
    private String coverImageUrl;

    @Schema(description = "聯絡 Email")
    private String email;

    @Schema(description = "聯絡電話")
    private String phone;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "營業時間")
    private String businessHours;

    @Schema(description = "Facebook 連結")
    private String facebookUrl;

    @Schema(description = "Instagram 連結")
    private String instagramUrl;

    @Schema(description = "LINE ID")
    private String lineId;

    @Schema(description = "上架商品列表")
    private List<LotteryListItemRes> products;
}
