package com.group.admin.gateway;

import com.group.admin.config.GoMyPayProperties;
import com.group.admin.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GoMyPaySupport 測試")
class GoMyPaySupportTest {

    @Test
    @DisplayName("正式設定若仍為 example.com 應直接擋下")
    void validatePaymentRequestConfig_ShouldRejectPlaceholderUrl() {
        GoMyPayProperties properties = new GoMyPayProperties();
        properties.setApiUrl("https://n.gomypay.asia/ShuntClass.aspx");
        properties.setShopId("shop123");
        properties.setHashKey("hash123");

        assertThatThrownBy(() -> GoMyPaySupport.validatePaymentRequestConfig(
                properties,
                "https://example.com/client/result",
                "https://api.example.com/callback"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不可使用預設或占位值");
    }

    @Test
    @DisplayName("Buyer_Name 與 Buyer_Memo 應清掉 route 片段與控制字元")
    void sanitizeText_ShouldStripBrowseFragmentAndControlChars() {
        String buyerName = GoMyPaySupport.sanitizeBuyerName("測試會員/browse/\n", "預設會員");
        String buyerMemo = GoMyPaySupport.sanitizeBuyerMemo("KUJI 訂單 /browse/\r\nABC", "備註");

        assertThat(buyerName).isEqualTo("測試會員");
        assertThat(buyerMemo).isEqualTo("KUJI 訂單 ABC");
    }

    @Test
    @DisplayName("callback 驗章應支援 PayAmount/e_money 與大小寫 str_check")
    void verifyCallback_ShouldSupportAmountFallbackAndCheckKeyVariants() {
        GoMyPayProperties properties = buildCallbackProperties();
        Map<String, String> params = Map.of(
                "result", "1",
                "e_orderno", "RC260514123456ABCDEF001",
                "e_money", "100",
                "OrderID", "2026051400000000001",
                "Str_Check", GoMyPaySupport.md5Hex(
                        "1" + "RC260514123456ABCDEF001" + "60530393" + "100"
                                + "2026051400000000001" + "hash-key"));

        GoMyPaySupport.verifyCallback(params, properties);
        assertThat(GoMyPaySupport.computeCallbackChecksum(params, properties)).isEqualTo(params.get("Str_Check"));
    }

    @Test
    @DisplayName("callback 缺少 str_check 應拒絕")
    void verifyCallback_ShouldRejectMissingCheck() {
        GoMyPayProperties properties = buildCallbackProperties();
        Map<String, String> params = Map.of(
                "result", "1",
                "e_orderno", "RC260514123456ABCDEF001",
                "PayAmount", "100",
                "OrderID", "2026051400000000001");

        assertThatThrownBy(() -> GoMyPaySupport.verifyCallback(params, properties))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("缺少 str_check");
    }

    @Test
    @DisplayName("商戶訂單編號不可超過 GoMyPay 25 字元限制")
    void validateMerchantOrderNo_ShouldRejectTooLongValue() {
        assertThatThrownBy(() -> GoMyPaySupport.validateMerchantOrderNo("A".repeat(26)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("25 字元");
    }

    private GoMyPayProperties buildCallbackProperties() {
        GoMyPayProperties properties = new GoMyPayProperties();
        properties.setShopId("encrypted-shop-id");
        properties.setVerifyCustomerId("60530393");
        properties.setHashKey("hash-key");
        return properties;
    }
}
