package com.group.admin.service;

import com.group.admin.entity.District;
import com.group.admin.exception.BusinessException;
import com.group.admin.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StoreAddressValidator {

    private final DistrictRepository districtRepository;

    public District requireValidDistrictAddress(String address) {
        String normalizedAddress = normalizeAddressText(address);
        if (normalizedAddress == null || isPlaceholderAddress(normalizedAddress)) {
            throw new BusinessException(
                    "STORE_ADDRESS_REQUIRED",
                    "店家地址必填，請選擇縣市、行政區並填寫詳細地址");
        }

        return districtRepository.selectAll().stream()
                .filter(district -> normalizedAddress.contains(normalizeAddressText(district.getCity()))
                        && normalizedAddress.contains(normalizeAddressText(district.getDistrictName())))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "STORE_ADDRESS_DISTRICT_INVALID",
                        "店家地址需包含有效縣市與行政區，請使用後台縣市、行政區選單填寫"));
    }

    private boolean isPlaceholderAddress(String normalizedAddress) {
        return "無".equals(normalizedAddress)
                || "沒有".equals(normalizedAddress)
                || "無實體店".equals(normalizedAddress)
                || "無實體店面".equals(normalizedAddress)
                || "NA".equalsIgnoreCase(normalizedAddress)
                || "N/A".equalsIgnoreCase(normalizedAddress);
    }

    private String normalizeAddressText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value
                .replace('臺', '台')
                .replaceAll("\\s+", "")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
