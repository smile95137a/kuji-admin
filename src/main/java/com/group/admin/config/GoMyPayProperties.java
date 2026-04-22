package com.group.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GoMyPay 金流設定（從 application.yml 讀取）
 *
 * payment.gateway.gomypay.* 對應本類別欄位
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.gateway.gomypay")
public class GoMyPayProperties {

    /** GoMyPay API 端點（測試或正式） */
    private String apiUrl = "https://n.gomypay.asia/TestShopApi.aspx";

    /** 商店代號 */
    private String shopId;

    /** Hash Key（加密用） */
    private String hashKey;

    /** Hash IV（加密用） */
    private String hashIv;

    /** 付款完成後跳轉至前台的 URL */
    private String returnUrl;

    /** GoMyPay 非同步通知後端的 URL */
    private String notifyUrl;
}
