package com.group.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "logistics.gomypay")
public class GoMyPayLogisticsProperties {

    private String logisticsApiUrl = "https://logistics.gomypay.asia/LogisticsAPI.aspx";

    private String homeApiUrl = "https://logistics.gomypay.asia/Api/Delivery/PrintOBT.aspx";

    private String storeSelectorUrl = "https://logistics.gomypay.asia/Logisticstm.aspx";

    private String eshopId;

    private String secret;

    private String callbackUrl;

    private String senderName;

    private String senderMobile;

    private String senderZipCode;

    private String senderAddress;

    private String productTypeId = "0015";

    private String productName = "KUJI獎品";

    private String homeTemperature = "0001";

    private String homeSpec = "0001";
}
