package com.group.admin.gateway;

import com.group.admin.config.GoMyPayProperties;
import com.group.admin.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
