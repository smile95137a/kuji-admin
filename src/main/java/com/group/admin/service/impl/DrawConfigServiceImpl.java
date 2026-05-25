package com.group.admin.service.impl;

import com.group.admin.service.DrawBonusTierConfigParser;
import com.group.admin.service.DrawConfigService;
import com.group.admin.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawConfigServiceImpl implements DrawConfigService {

    private static final String DEFAULT_BONUS_TITLE = "多抽優惠";
    private static final String DEFAULT_BONUS_DESCRIPTION = "現貨在倉，限定帶抽才會贈送。";
    private static final String DEFAULT_PROTECTION_TITLE = "保護期說明";
    private static final String DEFAULT_PROTECTION_DESCRIPTION = "第一次抽獎保護 300 秒，每次再抽額外延長 30 秒，上限 600 秒。";
    private static final String DEFAULT_BONUS_TIERS_TEXT = """
            3:60
            5:120
            8:180
            10:240
            """;

    private final SystemConfigService systemConfigService;
    private final DrawBonusTierConfigParser drawBonusTierConfigParser;

    @Override
    public int resolveProtectionSeconds(int drawCount) {
        int baseSeconds = systemConfigService.getInt(SystemConfigService.KEY_DRAW_PROTECTION_BASE_SECONDS, 300);
        int maxSeconds = systemConfigService.getInt(SystemConfigService.KEY_DRAW_PROTECTION_MAX_SECONDS, 600);
        return Math.min(baseSeconds, maxSeconds);
    }

    @Override
    public long resolveBonusForDrawCount(int drawCount) {
        return getBonusTiers().stream()
                .filter(tier -> tier.drawCount() == drawCount)
                .map(BonusTier::bonus)
                .findFirst()
                .orElse(0L);
    }

    @Override
    public List<BonusTier> getBonusTiers() {
        String raw = systemConfigService.getString(
                SystemConfigService.KEY_DRAW_BONUS_TIERS_JSON,
                DEFAULT_BONUS_TIERS_TEXT);
        try {
            return drawBonusTierConfigParser.parse(raw);
        } catch (Exception ex) {
            log.warn("解析多抽紅利階梯設定失敗，改用預設值: {}", ex.getMessage());
            return drawBonusTierConfigParser.parse(DEFAULT_BONUS_TIERS_TEXT);
        }
    }

    @Override
    public NoticeConfig getNoticeConfig() {
        return new NoticeConfig(
                systemConfigService.getString(SystemConfigService.KEY_DRAW_BONUS_TITLE, DEFAULT_BONUS_TITLE),
                systemConfigService.getString(SystemConfigService.KEY_DRAW_BONUS_DESCRIPTION, DEFAULT_BONUS_DESCRIPTION),
                systemConfigService.getString(SystemConfigService.KEY_DRAW_PROTECTION_TITLE, DEFAULT_PROTECTION_TITLE),
                systemConfigService.getString(SystemConfigService.KEY_DRAW_PROTECTION_DESCRIPTION, DEFAULT_PROTECTION_DESCRIPTION),
                getBonusTiers());
    }
}
