package com.group.admin.service.logistics;

import com.group.admin.config.GoMyPayLogisticsProperties;
import com.group.admin.entity.Order;
import com.group.admin.entity.ShippingMethod;
import com.group.admin.example.ShippingMethodExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.OrderMapper;
import com.group.admin.mapper.ShippingMethodMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "logistics.provider", havingValue = "gomypay")
public class GoMyPayLogisticsServiceImpl implements LogisticsService {

    private static final Pattern RESPONSE_PATTERN =
            Pattern.compile("Vendororder=(.*?),OrderNo=(.*?),ErrorCode=(.*?),ErrorMessage=(.*)");

    private final OrderMapper orderMapper;
    private final ShippingMethodMapper shippingMethodMapper;
    private final GoMyPayLogisticsProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ShippingResult createShipment(String orderId, ShippingInfo info) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }
        if (info == null) {
            throw new BusinessException("LOGISTICS_INFO_REQUIRED", "物流資訊不可為空");
        }

        ShippingMethod shippingMethod = resolveShippingMethod(order, info.getShippingMethodCode());
        validateRequiredConfig();

        String code = safeUpper(shippingMethod.getCode());
        if ("HOME_DELIVERY".equals(code)) {
            return createHomeShipment(order, info, shippingMethod);
        }
        if ("SEVEN_ELEVEN".equals(code) || "FAMILY_MART".equals(code)) {
            return createConvenienceShipment(order, info, shippingMethod);
        }

        throw new BusinessException("LOGISTICS_METHOD_NOT_SUPPORTED", "尚未支援的物流方式: " + shippingMethod.getCode());
    }

    @Override
    public ShippingResult queryShipment(String trackingNumber) {
        ShippingResult result = new ShippingResult();
        result.setSuccess(true);
        result.setTrackingNumber(trackingNumber);
        result.setProvider("GOMYPAY");
        result.setMessage("GoMyPay 物流狀態請改以 trackingUrl 查詢");
        return result;
    }

    @Override
    public List<ConvenienceStore> queryStores(String type, String city, String keyword) {
        return Collections.emptyList();
    }

    @Override
    public String createStoreSelectorUrl(String shippingMethodCode, String returnUrl) {
        if (isBlank(returnUrl)) {
            throw new BusinessException("STORE_SELECTOR_RETURN_URL_REQUIRED", "門市地圖返回網址不可為空");
        }

        String code = safeUpper(shippingMethodCode);
        if (!"SEVEN_ELEVEN".equals(code) && !"FAMILY_MART".equals(code)) {
            throw new BusinessException("STORE_SELECTOR_METHOD_NOT_SUPPORTED", "此物流方式不支援門市地圖");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("Url", returnUrl);
        params.add("Opmode", resolveOpMode(code));

        String responseBody = postForm(properties.getStoreSelectorUrl(), params);
        String selectorUrl = extractHrefUrl(responseBody);
        if (isBlank(selectorUrl)) {
            throw new BusinessException("STORE_SELECTOR_RESPONSE_INVALID", "物流平台未回傳門市地圖網址");
        }
        return selectorUrl;
    }

    private ShippingResult createConvenienceShipment(Order order, ShippingInfo info, ShippingMethod shippingMethod) {
        if (isBlank(info.getStoreCode())) {
            throw new BusinessException("STORE_CODE_REQUIRED", "超商取貨訂單缺少門市代碼");
        }
        if (isBlank(info.getRecipientName()) || isBlank(info.getRecipientPhone())) {
            throw new BusinessException("RECIPIENT_REQUIRED", "超商取貨訂單缺少收件人資訊");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("Vendororder", order.getOrderNumber());
        params.add("mode", "C2C");
        params.add("EshopId", properties.getEshopId());
        params.add("StoreId", info.getStoreCode());
        params.add("Amount", String.valueOf(resolveShipmentAmount(info)));
        params.add("ServiceType", "3");
        params.add("OrderAmount", String.valueOf(resolveShipmentAmount(info)));
        params.add("SenderName", properties.getSenderName());
        params.add("SendMobilePhone", properties.getSenderMobile());
        params.add("ReceiverName", info.getRecipientName());
        params.add("ReceiverMobilePhone", info.getRecipientPhone());
        params.add("OPMode", resolveOpMode(shippingMethod.getCode()));
        params.add("Internetsite", properties.getCallbackUrl());
        params.add("ShipDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        params.add("CHKMAC", computeChkMac(order.getOrderNumber()));

        String rawPayload = postForm(properties.getLogisticsApiUrl(), params);
        return parseShipmentResponse(rawPayload, shippingMethod, "GOMYPAY_CVS");
    }

    private ShippingResult createHomeShipment(Order order, ShippingInfo info, ShippingMethod shippingMethod) {
        if (isBlank(info.getRecipientName()) || isBlank(info.getRecipientPhone()) || isBlank(info.getRecipientAddress())) {
            throw new BusinessException("RECIPIENT_REQUIRED", "宅配訂單缺少完整收件資訊");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("VendorOrder", order.getOrderNumber());
        params.add("EshopId", properties.getEshopId());
        params.add("Thermosphere", properties.getHomeTemperature());
        params.add("Spec", properties.getHomeSpec());
        params.add("ServiceType", "3");
        params.add("InternetSite", properties.getCallbackUrl());
        params.add("Amount", String.valueOf(resolveShipmentAmount(info)));
        params.add("OrderAmount", String.valueOf(resolveShipmentAmount(info)));
        params.add("RecipientName", info.getRecipientName());
        params.add("RecipientMobile", info.getRecipientPhone());
        params.add("RecipientAddress", info.getRecipientAddress());
        params.add("SenderName", properties.getSenderName());
        params.add("SenderMobile", properties.getSenderMobile());
        params.add("SenderZipCode", properties.getSenderZipCode());
        params.add("SenderAddress", properties.getSenderAddress());
        params.add("ShipmentDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        params.add("DeliveryDate", LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE));
        params.add("DeliveryTime", "4");
        params.add("ProductTypeId", properties.getProductTypeId());
        params.add("PrintDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        params.add("ProductName", properties.getProductName());
        params.add("CHKMAC", computeChkMac(order.getOrderNumber()));

        String rawPayload = postForm(properties.getHomeApiUrl(), params);
        return parseShipmentResponse(rawPayload, shippingMethod, "GOMYPAY_HOME");
    }

    private ShippingResult parseShipmentResponse(String rawPayload, ShippingMethod shippingMethod, String provider) {
        if (isBlank(rawPayload)) {
            throw new BusinessException("LOGISTICS_EMPTY_RESPONSE", "物流平台未返回資料");
        }

        Matcher matcher = RESPONSE_PATTERN.matcher(rawPayload);
        if (!matcher.find()) {
            throw new BusinessException("LOGISTICS_RESPONSE_INVALID", "物流平台回應格式不正確: " + rawPayload);
        }

        String providerOrderNo = matcher.group(1).trim();
        String trackingNo = matcher.group(2).trim();
        String errorCode = matcher.group(3).trim();
        String errorMessage = matcher.group(4).trim();

        if (!"000".equals(errorCode)) {
            throw new BusinessException("LOGISTICS_CREATE_FAILED", String.format("物流建立失敗[%s]: %s", errorCode, errorMessage));
        }

        ShippingResult result = new ShippingResult();
        result.setSuccess(true);
        result.setTrackingNumber(trackingNo);
        result.setTrackingUrl(buildTrackingUrl(shippingMethod, trackingNo));
        result.setProviderOrderNo(providerOrderNo);
        result.setProvider(provider);
        result.setRawPayload(rawPayload);
        result.setMessage(errorMessage);
        return result;
    }

    private String postForm(String url, MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        log.info("GoMyPay logistics request sent to {} keys={}", url, params.keySet());
        return response.getBody();
    }

    private String extractHrefUrl(String html) {
        if (isBlank(html)) {
            return null;
        }
        Matcher matcher = Pattern.compile("href=\"(.*?)\"").matcher(html);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).replace("&amp;", "&");
    }

    private ShippingMethod resolveShippingMethod(Order order, String shippingMethodCode) {
        if (!isBlank(order.getShippingMethodId())) {
            ShippingMethod method = shippingMethodMapper.selectByPrimaryKey(order.getShippingMethodId());
            if (method != null) {
                return method;
            }
        }

        String targetCode = !isBlank(shippingMethodCode) ? shippingMethodCode : order.getShippingMethod();
        ShippingMethodExample example = new ShippingMethodExample();
        example.createCriteria().andCodeEqualTo(targetCode);
        return shippingMethodMapper.selectByExample(example).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("SHIPPING_METHOD_NOT_FOUND", "找不到物流方式: " + targetCode));
    }

    private void validateRequiredConfig() {
        if (isBlank(properties.getEshopId())
                || isBlank(properties.getSecret())
                || isBlank(properties.getCallbackUrl())
                || isBlank(properties.getSenderName())
                || isBlank(properties.getSenderMobile())) {
            throw new BusinessException("LOGISTICS_CONFIG_INCOMPLETE", "GoMyPay 物流設定不完整，請先設定 logistics.gomypay.*");
        }
    }

    private long resolveShipmentAmount(ShippingInfo info) {
        Long amount = info.getAmount();
        return amount != null && amount > 0 ? amount : 1L;
    }

    private String resolveOpMode(String shippingMethodCode) {
        return switch (safeUpper(shippingMethodCode)) {
            case "FAMILY_MART" -> "1";
            case "SEVEN_ELEVEN" -> "3";
            default -> "3";
        };
    }

    private String buildTrackingUrl(ShippingMethod shippingMethod, String trackingNo) {
        if (isBlank(trackingNo)) {
            return null;
        }
        return switch (safeUpper(shippingMethod.getCode())) {
            case "HOME_DELIVERY" ->
                    "https://www.t-cat.com.tw/Inquire/TraceDetail.aspx?BillID=" + trackingNo;
            case "SEVEN_ELEVEN" ->
                    "https://eservice.7-11.com.tw/e-tracking/search.aspx?TBSTKECNO=" + trackingNo;
            case "FAMILY_MART" ->
                    "https://www.famiport.com.tw/Web_Famiport/page/process.aspx?PGMID=ORDERQUERY";
            default -> null;
        };
    }

    private String computeChkMac(String vendorOrder) {
        String source = (properties.getSecret() + vendorOrder).toLowerCase(Locale.ROOT);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02X", b));
            }
            return result.toString();
        } catch (Exception ex) {
            throw new BusinessException("LOGISTICS_CHKMAC_ERROR", "物流檢查碼計算失敗");
        }
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
