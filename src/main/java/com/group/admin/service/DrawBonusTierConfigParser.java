package com.group.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DrawBonusTierConfigParser {

    private static final Pattern STRICT_TEXT_TIER_PATTERN = Pattern.compile(
            "(\\d+)\\s*(?:抽|draws?)?\\s*(?:[:=：])\\s*(\\d+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern LOOSE_TEXT_TIER_PATTERN = Pattern.compile("(\\d+)\\D+(\\d+)");

    private final ObjectMapper objectMapper;

    public List<DrawConfigService.BonusTier> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("多抽紅利設定不可空白");
        }

        String normalized = raw.trim();
        if (normalized.startsWith("[")) {
            return parseJson(normalized);
        }

        return parseSimpleText(normalized);
    }

    private List<DrawConfigService.BonusTier> parseJson(String raw) {
        try {
            List<DrawConfigService.BonusTier> tiers = objectMapper.readValue(
                    raw,
                    new TypeReference<List<DrawConfigService.BonusTier>>() {
                    });
            return normalize(tiers);
        } catch (Exception ex) {
            throw new IllegalArgumentException("多抽紅利設定 JSON 格式錯誤", ex);
        }
    }

    private List<DrawConfigService.BonusTier> parseSimpleText(String raw) {
        List<DrawConfigService.BonusTier> tiers = new ArrayList<>();
        Matcher strictMatcher = STRICT_TEXT_TIER_PATTERN.matcher(raw);
        while (strictMatcher.find()) {
            tiers.add(toTier(strictMatcher.group(1), strictMatcher.group(2)));
        }

        if (!tiers.isEmpty()) {
            return normalize(tiers);
        }

        String[] segments = raw.split("[\\r\\n,，、;；]+");
        for (String segment : segments) {
            String item = segment == null ? "" : segment.trim();
            if (item.isBlank()) {
                continue;
            }

            Matcher looseMatcher = LOOSE_TEXT_TIER_PATTERN.matcher(item);
            if (!looseMatcher.find()) {
                throw new IllegalArgumentException("多抽紅利設定格式錯誤，請使用 3:70 或 3抽 = 70");
            }

            tiers.add(toTier(looseMatcher.group(1), looseMatcher.group(2)));
        }

        return normalize(tiers);
    }

    private DrawConfigService.BonusTier toTier(String drawCount, String bonus) {
        return new DrawConfigService.BonusTier(
                Integer.parseInt(drawCount),
                Long.parseLong(bonus));
    }

    private List<DrawConfigService.BonusTier> normalize(List<DrawConfigService.BonusTier> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("多抽紅利設定不可空白");
        }

        Map<Integer, DrawConfigService.BonusTier> uniqueTiers = new LinkedHashMap<>();
        for (DrawConfigService.BonusTier tier : tiers) {
            if (tier != null && tier.drawCount() > 0 && tier.bonus() > 0) {
                uniqueTiers.put(tier.drawCount(), tier);
            }
        }

        List<DrawConfigService.BonusTier> normalized = uniqueTiers.values().stream()
                .sorted(Comparator.comparingInt(DrawConfigService.BonusTier::drawCount))
                .toList();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("多抽紅利設定至少需要一組有效級距");
        }

        return normalized;
    }
}
