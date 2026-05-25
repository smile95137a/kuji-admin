package com.group.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DrawBonusTierConfigParserTest {

    private final DrawBonusTierConfigParser parser = new DrawBonusTierConfigParser(new ObjectMapper());

    @Test
    @DisplayName("可解析後台顯示格式的多抽紅利設定")
    void parseHumanReadableBonusTiers() {
        var tiers = parser.parse("3抽 = 70、5抽 = 120、8抽 = 180、10抽 = 240");

        assertThat(tiers)
                .extracting(DrawConfigService.BonusTier::drawCount, DrawConfigService.BonusTier::bonus)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(3, 70L),
                        org.assertj.core.groups.Tuple.tuple(5, 120L),
                        org.assertj.core.groups.Tuple.tuple(8, 180L),
                        org.assertj.core.groups.Tuple.tuple(10, 240L));
    }

    @Test
    @DisplayName("可解析簡易冒號格式的多抽紅利設定")
    void parseColonBonusTiers() {
        var tiers = parser.parse("""
                3:70
                5:120
                8:180
                10:240
                """);

        assertThat(tiers).hasSize(4);
        assertThat(tiers.get(0).bonus()).isEqualTo(70L);
    }
}
