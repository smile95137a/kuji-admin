package com.group.admin.service.logistics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.config.GoMyPayLogisticsProperties;
import com.group.admin.entity.District;
import com.group.admin.entity.Order;
import com.group.admin.entity.ShippingMethod;
import com.group.admin.entity.Store;
import com.group.admin.example.ShippingMethodExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.OrderMapper;
import com.group.admin.mapper.ShippingMethodMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.repository.DistrictRepository;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "logistics.provider", havingValue = "gomypay")
public class GoMyPayLogisticsServiceImpl implements LogisticsService {

    private static final Pattern CVS_RESPONSE_PATTERN =
            Pattern.compile("Vendororder=(.*?),OrderNo=(.*?),ErrorCode=(.*?),ErrorMessage=(.*)", Pattern.DOTALL);

    private final OrderMapper orderMapper;
    private final ShippingMethodMapper shippingMethodMapper;
    private final StoreMapper storeMapper;
    private final DistrictRepository districtRepository;
    private final GoMyPayLogisticsProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ShippingResult createShipment(String orderId, ShippingInfo info) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }
        if (info == null) {
            throw new BusinessException("LOGISTICS_INFO_REQUIRED", "請提供物流資訊");
        }

        ShippingMethod shippingMethod = resolveShippingMethod(order, info.getShippingMethodCode());
        validateRequiredConfig();

        String code = safeUpper(shippingMethod.getCode());
        if ("HOME_DELIVERY".equals(code)) {
            return createSfShipment(order, info);
        }
        if (isConvenienceStore(code)) {
            return createConvenienceShipment(order, info, shippingMethod);
        }

        throw new BusinessException("LOGISTICS_METHOD_NOT_SUPPORTED", "尚未支援的物流方式: " + shippingMethod.getCode());
    }

    @Override
    public ShippingResult queryShipment(String trackingNumber) {
        if (isBlank(trackingNumber)) {
            throw new BusinessException("TRACKING_NO_REQUIRED", "請提供物流單號");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("OrderNo", trackingNumber);
        params.add("ReturnUrl", properties.getCallbackUrl());

        String rawPayload = postForm(properties.getStatusCheckUrl(), params);
        return ShippingResult.builder()
                .success(true)
                .trackingNumber(trackingNumber)
                .provider("GOMYPAY")
                .rawPayload(rawPayload)
                .message("物流狀態查詢完成")
                .build();
    }

    @Override
    public List<ConvenienceStore> queryStores(String type, String city, String keyword) {
        return Collections.emptyList();
    }

    @Override
    public String createStoreSelectorUrl(String shippingMethodCode, String returnUrl) {
        if (isBlank(returnUrl)) {
            throw new BusinessException("STORE_SELECTOR_RETURN_URL_REQUIRED", "請提供超商選店回傳網址");
        }

        String code = safeUpper(shippingMethodCode);
        if (!isConvenienceStore(code)) {
            throw new BusinessException("STORE_SELECTOR_METHOD_NOT_SUPPORTED", "此物流方式不支援超商選店");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("Url", returnUrl);
        params.add("Opmode", resolveOpMode(code));

        String responseBody = postForm(properties.getStoreSelectorUrl(), params);
        String selectorUrl = extractHrefUrl(responseBody);
        if (isBlank(selectorUrl)) {
            throw new BusinessException("STORE_SELECTOR_RESPONSE_INVALID", "GoMyPay 未回傳有效的超商選店網址");
        }
        return selectorUrl;
    }

    @Override
    public String handleStatusCallback(Map<String, String> params) {
        String eshopId = value(params, "EshopId");
        String orderNo = value(params, "OrderNo");
        String status = value(params, "status");

        if (!isBlank(properties.getEshopId()) && !properties.getEshopId().equals(eshopId)) {
            log.warn("GoMyPay logistics callback ignored due to eshop mismatch: {}", params);
            return String.format("%s,%s,999", eshopId, orderNo);
        }
        if (isBlank(orderNo)) {
            log.warn("GoMyPay logistics callback missing OrderNo: {}", params);
            return String.format("%s,%s,999", reqValue(eshopId, properties.getEshopId(), ""), "");
        }

        int updated = orderMapper.updateLogisticsStatusByTrackingNo(orderNo, status, mapStatusName(status));
        log.info("GoMyPay logistics callback handled: orderNo={}, status={}, updated={}", orderNo, status, updated);
        return String.format("%s,%s,000", reqValue(eshopId, properties.getEshopId(), ""), orderNo);
    }

    private ShippingResult createConvenienceShipment(Order order, ShippingInfo info, ShippingMethod shippingMethod) {
        if (isBlank(info.getStoreCode())) {
            throw new BusinessException("STORE_CODE_REQUIRED", "超商取貨需先完成選店");
        }
        if (isBlank(info.getRecipientName()) || isBlank(info.getRecipientPhone())) {
            throw new BusinessException("RECIPIENT_REQUIRED", "請提供收件人姓名與手機");
        }

        long amount = resolveShipmentAmount(info);
        SenderInfo sender = resolveSenderInfo(order, false);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("Vendororder", order.getOrderNumber());
        params.add("mode", "C2C");
        params.add("EshopId", properties.getEshopId());
        params.add("StoreId", info.getStoreCode());
        params.add("Amount", String.valueOf(amount));
        params.add("ServiceType", "3");
        params.add("OrderAmount", String.valueOf(amount));
        params.add("SenderName", sender.name());
        params.add("SendMobilePhone", normalizePhone(sender.mobile()));
        params.add("ReceiverName", trimToMax(info.getRecipientName(), 20));
        params.add("ReceiverMobilePhone", normalizePhone(info.getRecipientPhone()));
        params.add("OPMode", resolveOpMode(shippingMethod.getCode()));
        params.add("Internetsite", properties.getCallbackUrl());
        params.add("ShipDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        params.add("PRODUCTTYPE", properties.getProductTypeId());
        params.add("CHKMAC", computeChkMac(order.getOrderNumber()));

        String rawPayload = postForm(properties.getLogisticsApiUrl(), params);
        return parseConvenienceShipmentResponse(rawPayload, shippingMethod);
    }

    private ShippingResult createSfShipment(Order order, ShippingInfo info) {
        if (isBlank(info.getRecipientName()) || isBlank(info.getRecipientPhone()) || isBlank(info.getRecipientAddress())) {
            throw new BusinessException("RECIPIENT_REQUIRED", "宅配到府需提供完整收件人資訊與地址");
        }
        SenderInfo sender = resolveSenderInfo(order, true);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("CustomerOrderNo", order.getOrderNumber());
        params.add("EshopId", properties.getEshopId());
        params.add("DeclaredValue", String.valueOf(resolveShipmentAmount(info)));
        params.add("TotalWeight", properties.getSfTotalWeight());
        params.add("PickupType", properties.getSfPickupType());
        params.add("Amount", String.valueOf(resolveShipmentAmount(info)));
        params.add("Name", properties.getProductName());
        params.add("Quantity", properties.getSfQuantity());
        params.add("ReceiverContact", trimToMax(info.getRecipientName(), 20));
        params.add("ReceiverPhone", normalizePhone(info.getRecipientPhone()));
        params.add("ReceiverRegion", "TW");
        params.add("ReceiverAdress", info.getRecipientAddress());
        params.add("ReceiverAddress", info.getRecipientAddress());
        params.add("ReceiverPostcode", reqValue(resolvePostalCode(info.getRecipientAddress()), null, "000"));
        params.add("SenderContact", trimToMax(sender.name(), 20));
        params.add("SenderPhone", normalizePhone(sender.mobile()));
        params.add("SenderRegion", sender.region());
        params.add("SenderAdress", sender.address());
        params.add("SenderAddress", sender.address());
        params.add("SenderPodtcode", sender.zipCode());
        params.add("SenderPostcode", sender.zipCode());
        params.add("Remark", "KUJI 賞品出貨，寄件店家：" + sender.name());
        params.add("CHKMAC", computeChkMac(order.getOrderNumber()));

        String rawPayload = postForm(properties.getSfCreateApiUrl(), params);
        Map<String, Object> data = parseObjectResponse(rawPayload);
        String sfWaybillNo = firstText(data,
                "SfWaybillNo", "SFWaybillNo", "sfWaybillNo", "WaybillNo", "waybillNo", "OrderNo", "orderNo");
        String message = firstText(data, "Msg", "msg", "Message", "message", "ErrorMessage", "errorMessage", "ret_msg");
        String errorCode = firstText(data, "ErrorCode", "errorCode", "Code", "code", "result", "Result");
        String errorMessage = firstText(data, "ErrorMessage", "errorMessage", "ErrMsg", "errMsg");

        if (isFailedCode(errorCode) || isBlank(sfWaybillNo) || !isBlank(errorMessage)) {
            throw new BusinessException("LOGISTICS_CREATE_FAILED",
                    reqValue(errorMessage, message,
                            "順豐物流單建立失敗，GoMyPay 未回傳物流單號，原始回應: " + trimToMax(rawPayload, 200)));
        }

        String labelUrl = requestSfPrintUrl(sfWaybillNo);
        return ShippingResult.builder()
                .success(true)
                .trackingNumber(sfWaybillNo)
                .trackingUrl(buildSfTrackingUrl(sfWaybillNo))
                .labelUrl(labelUrl)
                .providerOrderNo(order.getOrderNumber())
                .provider("SF")
                .statusCode("CREATED")
                .statusName("已建立")
                .rawPayload(rawPayload)
                .message(reqValue(message, null, "順豐物流單建立成功"))
                .build();
    }

    private ShippingResult parseConvenienceShipmentResponse(String rawPayload, ShippingMethod shippingMethod) {
        if (isBlank(rawPayload)) {
            throw new BusinessException("LOGISTICS_EMPTY_RESPONSE", "GoMyPay 未回傳物流建立結果");
        }

        Matcher matcher = CVS_RESPONSE_PATTERN.matcher(rawPayload);
        if (!matcher.find()) {
            throw new BusinessException("LOGISTICS_RESPONSE_INVALID", "GoMyPay 物流回應格式不正確: " + rawPayload);
        }

        String providerOrderNo = matcher.group(1).trim();
        String trackingNo = matcher.group(2).trim();
        String errorCode = matcher.group(3).trim();
        String errorMessage = matcher.group(4).trim();

        if (!"000".equals(errorCode)) {
            throw new BusinessException("LOGISTICS_CREATE_FAILED", String.format("GoMyPay 物流建立失敗[%s]: %s", errorCode, errorMessage));
        }

        return ShippingResult.builder()
                .success(true)
                .trackingNumber(trackingNo)
                .trackingUrl(buildTrackingUrl(shippingMethod, trackingNo))
                .labelUrl(buildConveniencePrintUrl(trackingNo))
                .providerOrderNo(providerOrderNo)
                .provider("GOMYPAY_CVS")
                .statusCode(errorCode)
                .statusName("已建立")
                .rawPayload(rawPayload)
                .message(errorMessage)
                .build();
    }

    private String requestSfPrintUrl(String sfWaybillNo) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("SfWaybillNo", sfWaybillNo);
            String rawPayload = postForm(properties.getSfPrintApiUrl(), params);
            Map<String, Object> data = parseJsonObject(rawPayload);
            return firstText(data, "PDFUrl", "pdfUrl");
        } catch (Exception ex) {
            log.warn("SF label request failed, waybillNo={}", sfWaybillNo, ex);
            return null;
        }
    }

    private String postForm(String url, MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.ALL));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        String body = response.getBody();
        log.info("GoMyPay logistics request sent to {} status={} contentType={} keys={} body={}",
                url,
                response.getStatusCode(),
                response.getHeaders().getContentType(),
                params.keySet(),
                trimToMax(body, 500));
        if (isBlank(body)) {
            throw new BusinessException("LOGISTICS_EMPTY_RESPONSE",
                    "GoMyPay 物流 API 無回應（HTTP " + response.getStatusCode().value()
                            + "），請確認物流 API URL、店家代號、簽章與順豐欄位設定");
        }
        return body;
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
                .orElseThrow(() -> new BusinessException("SHIPPING_METHOD_NOT_FOUND", "查無物流方式: " + targetCode));
    }

    private void validateRequiredConfig() {
        List<String> missing = new ArrayList<>();
        if (isBlank(properties.getEshopId())) {
            missing.add("logistics.gomypay.eshop-id 或 GOMYPAY_SHOP_ID");
        }
        if (isBlank(properties.getSecret())) {
            missing.add("logistics.gomypay.secret 或 GOMYPAY_HASH_KEY");
        }
        if (isBlank(properties.getCallbackUrl())) {
            missing.add("logistics.gomypay.callback-url");
        }
        if (!missing.isEmpty()) {
            throw new BusinessException("LOGISTICS_CONFIG_INCOMPLETE", "GoMyPay 物流設定不完整，缺少: " + String.join(", ", missing));
        }
    }

    private SenderInfo resolveSenderInfo(Order order, boolean requireAddress) {
        Store store = !isBlank(order.getStoreId()) ? storeMapper.selectByPrimaryKey(order.getStoreId()) : null;
        String storeName = store != null ? store.getStoreName() : null;
        String senderName = reqValue(storeName, properties.getSenderName(), "KUJI一番賞");
        String senderMobile = reqValue(store != null ? store.getPhone() : null, properties.getSenderMobile(), null);
        String senderAddress = reqValue(store != null ? store.getAddress() : null, properties.getSenderAddress(), null);
        String senderZipCode = reqValue(resolvePostalCode(senderAddress), properties.getSenderZipCode(), null);
        String senderRegion = reqValue(properties.getSenderRegion(), null, "TW");

        List<String> missing = new ArrayList<>();
        if (isBlank(senderName)) {
            missing.add("店家名稱");
        }
        if (isBlank(senderMobile)) {
            missing.add("店家電話");
        }
        if (requireAddress && isBlank(senderAddress)) {
            missing.add("店家寄件/退貨地址");
        }
        if (requireAddress && isBlank(senderZipCode)) {
            missing.add("郵遞區號（請確認店家地址包含縣市與行政區，例如：台北市中山區，或設定 GOMYPAY_LOGISTICS_SENDER_ZIP_CODE 備援）");
        }
        if (!missing.isEmpty()) {
            String name = reqValue(storeName, order.getStoreId(), "未知店家");
            throw new BusinessException("LOGISTICS_SENDER_INFO_INCOMPLETE",
                    "店家「" + name + "」物流寄件資料不完整，缺少: " + String.join(", ", missing));
        }

        return new SenderInfo(senderName, senderMobile, senderRegion, senderZipCode, senderAddress);
    }

    private long resolveShipmentAmount(ShippingInfo info) {
        Long amount = info.getAmount();
        return amount != null && amount > 0 ? amount : 1L;
    }

    private String resolveOpMode(String shippingMethodCode) {
        return switch (safeUpper(shippingMethodCode)) {
            case "FAMILY_MART" -> "1";
            case "HI_LIFE" -> "2";
            case "SEVEN_ELEVEN" -> "3";
            case "OK_MART", "OK" -> "4";
            default -> "3";
        };
    }

    private boolean isConvenienceStore(String shippingMethodCode) {
        return switch (safeUpper(shippingMethodCode)) {
            case "SEVEN_ELEVEN", "FAMILY_MART", "HI_LIFE", "OK_MART", "OK" -> true;
            default -> false;
        };
    }

    private String buildTrackingUrl(ShippingMethod shippingMethod, String trackingNo) {
        if (shippingMethod == null || isBlank(trackingNo)) {
            return null;
        }
        return switch (safeUpper(shippingMethod.getCode())) {
            case "HOME_DELIVERY" -> buildSfTrackingUrl(trackingNo);
            case "SEVEN_ELEVEN" -> "https://eservice.7-11.com.tw/e-tracking/search.aspx?TBSTKECNO=" + trackingNo;
            case "FAMILY_MART" -> "https://www.famiport.com.tw/Web_Famiport/page/process.aspx?PGMID=ORDERQUERY";
            default -> null;
        };
    }

    private String buildSfTrackingUrl(String trackingNo) {
        return "https://www.sf-express.com/tw/tc/dynamic_function/waybill/#search/bill-number/" + trackingNo;
    }

    private String buildConveniencePrintUrl(String trackingNo) {
        if (isBlank(properties.getPrintUrl()) || isBlank(trackingNo)) {
            return null;
        }
        return properties.getPrintUrl() + "?OrderNo=" + URLEncoder.encode(trackingNo, StandardCharsets.UTF_8);
    }

    private Map<String, Object> parseJsonObject(String rawPayload) {
        if (isBlank(rawPayload)) {
            throw new BusinessException("LOGISTICS_EMPTY_RESPONSE", "GoMyPay 未回傳物流建立結果");
        }
        try {
            return objectMapper.readValue(rawPayload, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new BusinessException("LOGISTICS_RESPONSE_INVALID", "GoMyPay JSON 回應格式不正確: " + rawPayload);
        }
    }

    private Map<String, Object> parseObjectResponse(String rawPayload) {
        if (isBlank(rawPayload)) {
            throw new BusinessException("LOGISTICS_EMPTY_RESPONSE", "GoMyPay 未回傳物流建立結果");
        }

        String trimmed = rawPayload.trim();
        if (trimmed.startsWith("{")) {
            return parseJsonObject(trimmed);
        }

        Map<String, Object> keyValues = parseKeyValueResponse(trimmed);
        if (!keyValues.isEmpty()) {
            return keyValues;
        }

        throw new BusinessException("LOGISTICS_RESPONSE_INVALID", "GoMyPay 物流回應格式不正確: " + rawPayload);
    }

    private Map<String, Object> parseKeyValueResponse(String rawPayload) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (String token : rawPayload.split("[,&\\r\\n]+")) {
            int idx = token.indexOf('=');
            if (idx <= 0) {
                idx = token.indexOf(':');
            }
            if (idx <= 0) {
                continue;
            }

            String key = token.substring(0, idx).trim();
            String value = token.substring(idx + 1).trim();
            if (!key.isEmpty()) {
                result.put(key, value);
            }
        }
        return result;
    }

    private boolean isFailedCode(String code) {
        if (isBlank(code)) {
            return false;
        }
        String normalized = code.trim();
        return !("000".equals(normalized)
                || "0".equals(normalized)
                || "true".equalsIgnoreCase(normalized)
                || "success".equalsIgnoreCase(normalized));
    }

    private String firstText(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null && !isBlank(String.valueOf(value))) {
                return String.valueOf(value).trim();
            }
        }
        return null;
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
            throw new BusinessException("LOGISTICS_CHKMAC_ERROR", "物流簽章產生失敗");
        }
    }

    private String normalizePhone(String value) {
        return value == null ? "" : value.replaceAll("[^0-9+]", "");
    }

    private String resolvePostalCode(String address) {
        String explicitCode = extractPostalCode(address);
        if (!isBlank(explicitCode)) {
            return explicitCode;
        }
        if (isBlank(address)) {
            return null;
        }

        try {
            String normalizedAddress = normalizeAddressText(address);
            return districtRepository.selectAll().stream()
                    .filter(district -> !isBlank(district.getZipCode()))
                    .filter(district -> addressContainsDistrict(normalizedAddress, district))
                    .findFirst()
                    .map(District::getZipCode)
                    .orElse(null);
        } catch (Exception ex) {
            log.warn("Resolve postal code failed, address={}", address, ex);
            return null;
        }
    }

    private boolean addressContainsDistrict(String normalizedAddress, District district) {
        String city = normalizeAddressText(district.getCity());
        String districtName = normalizeAddressText(district.getDistrictName());
        return !isBlank(city)
                && !isBlank(districtName)
                && normalizedAddress.contains(city)
                && normalizedAddress.contains(districtName);
    }

    private String extractPostalCode(String value) {
        if (isBlank(value)) {
            return null;
        }
        Matcher matcher = Pattern.compile("(^|\\D)(\\d{3})(\\D|$)").matcher(value.trim());
        return matcher.find() ? matcher.group(2) : null;
    }

    private String normalizeAddressText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replace("臺", "台")
                .replace(" ", "")
                .replace("　", "");
    }

    private String trimToMax(String value, int max) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        return text.length() <= max ? text : text.substring(0, max);
    }

    private String mapStatusName(String status) {
        if (isBlank(status)) {
            return "狀態更新";
        }
        return switch (status) {
            case "1" -> "已寄件";
            case "2" -> "配送中";
            case "3" -> "已配達";
            case "4" -> "已取貨";
            case "5" -> "退貨中";
            case "6" -> "已退回";
            default -> "物流狀態 " + status;
        };
    }

    private String value(Map<String, String> params, String key) {
        if (params == null) {
            return null;
        }
        return params.get(key);
    }

    private String reqValue(String preferred, String fallback, String defaultValue) {
        if (!isBlank(preferred)) {
            return preferred;
        }
        if (!isBlank(fallback)) {
            return fallback;
        }
        return defaultValue;
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record SenderInfo(String name, String mobile, String region, String zipCode, String address) {
    }
}
