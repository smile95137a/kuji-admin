package com.group.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "logistics.gomypay")
public class GoMyPayLogisticsProperties {

    private String logisticsApiUrl = "https://logistics.gomypay.asia/LogisticsAPI.aspx";

    private String statusCheckUrl = "https://logistics.gomypay.asia/LogisticsStatusCheck.aspx";

    private String printUrl = "https://logistics.gomypay.asia/LogisticsPrint.aspx";

    private String storeSelectorUrl = "https://logistics.gomypay.asia/Logisticstm.aspx";

    private String sfCreateApiUrl = "https://logistics.gomypay.asia/Api/SF/SFCreateAPI.aspx";

    private String sfPrintApiUrl = "https://logistics.gomypay.asia/Api/SF/SFPrintAPI.aspx";

    private String sfTrackApiUrl = "https://logistics.gomypay.asia/Api/SF/SFOrderTrack.aspx";

    private String eshopId;

    private String secret;

    private String callbackUrl;

    private String senderName;

    private String senderMobile;

    private String senderRegion = "TW";

    private String senderZipCode;

    private String senderAddress;

    private String productTypeId = "0015";

    private String productName = "KUJI 賞品";

    private String sfPickupType = "0";

    private String sfTotalWeight = "1";

    private String sfQuantity = "1";
}
