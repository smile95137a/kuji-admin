package com.group.admin.service;

import java.util.List;

public interface DrawConfigService {

    record BonusTier(int drawCount, long bonus) {
    }

    record NoticeConfig(
            String bonusTitle,
            String bonusDescription,
            String protectionTitle,
            String protectionDescription,
            List<BonusTier> bonusTiers) {
    }

    int resolveProtectionSeconds(int drawCount);

    long resolveBonusForDrawCount(int drawCount);

    List<BonusTier> getBonusTiers();

    NoticeConfig getNoticeConfig();
}
