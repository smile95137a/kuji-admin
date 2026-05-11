package com.group.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.condition.CategoryCondition;
import com.group.admin.entity.LotteryTag;
import com.group.admin.entity.LotteryTheme;
import com.group.admin.entity.LotteryThemeAlias;
import com.group.admin.entity.Lottery;
import com.group.admin.exception.BusinessException;
import com.group.admin.example.LotteryExample;
import com.group.admin.mapper.LotteryTagMapper;
import com.group.admin.mapper.LotteryThemeAliasMapper;
import com.group.admin.mapper.LotteryThemeMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.category.CategoryHealthRes;
import com.group.admin.res.category.CategoryRes;
import com.group.admin.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品類別服務實作
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String CATEGORY_OFFICIAL_ICHIBAN = "OFFICIAL_ICHIBAN";
    private static final String CATEGORY_CUSTOM_GACHA = "CUSTOM_GACHA";
    private static final String CATEGORY_GACHA = "GACHA";
    private static final String CATEGORY_TRADING_CARD = "TRADING_CARD";
    private static final String MODE_SCRATCH = "SCRATCH_MODE";

    private static final List<DisplayCategoryBucket> DISPLAY_CATEGORY_BUCKETS = List.of(
            new DisplayCategoryBucket(CATEGORY_OFFICIAL_ICHIBAN, "官方一番賞", 10),
            new DisplayCategoryBucket(CATEGORY_CUSTOM_GACHA, "自製一番賞", 20),
            new DisplayCategoryBucket(MODE_SCRATCH, "刮刮樂", 30),
            new DisplayCategoryBucket(CATEGORY_GACHA, "扭蛋", 40),
            new DisplayCategoryBucket(CATEGORY_TRADING_CARD, "卡牌", 50));
    
    private final LotteryMapper lotteryMapper;
    private final LotteryThemeMapper lotteryThemeMapper;
    private final LotteryTagMapper lotteryTagMapper;
    private final LotteryThemeAliasMapper lotteryThemeAliasMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    public List<CategoryRes> queryCategories(QueryReq<CategoryCondition> req) {
        log.info("🔍 查詢商品類別");
        
        CategoryCondition condition = req != null ? req.getCondition() : null;
        
        // 查詢所有商品
        LotteryExample example = buildExample(condition);
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);
        
        // 按 category 分組
        Map<String, List<Lottery>> grouped = lotteries.stream()
                .filter(l -> l.getCategory() != null && !l.getCategory().trim().isEmpty())
                .collect(Collectors.groupingBy(Lottery::getCategory));
        
        // 轉換為 CategoryRes
        List<CategoryRes> result = grouped.entrySet().stream()
                .map(entry -> buildCategoryRes(entry.getKey(), "category", entry.getValue()))
                .sorted(Comparator.comparing(CategoryRes::getName))
                .collect(Collectors.toList());
        
        log.info("✅ 查詢完成，共 {} 個類別", result.size());
        return result;
    }

    @Override
    public List<CategoryRes> queryDisplayCategories(QueryReq<CategoryCondition> req) {
        log.info("🔍 查詢前台顯示分類（聚合）");

        CategoryCondition condition = req != null ? req.getCondition() : null;
        LotteryExample example = buildExample(condition);
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);

        Map<String, List<Lottery>> grouped = lotteries.stream()
                .map(lottery -> Map.entry(resolveDisplayCategoryKey(lottery), lottery))
                .filter(entry -> isNotBlank(entry.getKey()))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        List<CategoryRes> result = DISPLAY_CATEGORY_BUCKETS.stream()
                .map(bucket -> toDisplayCategoryRes(bucket, grouped.getOrDefault(bucket.key(), List.of())))
                .sorted(Comparator.comparing(CategoryRes::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        log.info("✅ 前台顯示分類查詢完成，共 {} 個分類", result.size());
        return result;
    }
    
    @Override
    public List<CategoryRes> queryThemes(QueryReq<CategoryCondition> req) {
        log.info("🎨 查詢商品主題");

        CategoryCondition condition = req != null ? req.getCondition() : null;

        LotteryExample example = buildExample(condition);
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);

        Map<String, List<Lottery>> grouped = lotteries.stream()
                .filter(l -> isNotBlank(l.getTheme()))
                .collect(Collectors.groupingBy(l -> resolveCanonicalThemeName(l.getTheme())));

        String themeStatus = resolveRequestedStatus(condition != null ? condition.getStatus() : null, true);
        List<LotteryTheme> themes = lotteryThemeMapper.selectAll(themeStatus);
        List<CategoryRes> result = new ArrayList<>();

        for (LotteryTheme theme : themes) {
            if (condition != null && isNotBlank(condition.getTheme())
                    && !theme.getName().equals(resolveCanonicalThemeName(condition.getTheme()))) {
                continue;
            }
            if (condition != null && isNotBlank(condition.getKeyword())
                    && !matchesKeyword(theme.getName(), condition.getKeyword())) {
                continue;
            }

            List<Lottery> matched = grouped.getOrDefault(theme.getName(), List.of());
            long hotCount = matched.stream()
                .mapToLong(l -> l.getHotCount() != null ? l.getHotCount() : 0)
                .sum();

            String imageUrl = isNotBlank(theme.getImageUrl())
                ? theme.getImageUrl()
                : (!matched.isEmpty() ? matched.get(0).getImageUrl() : null);

            result.add(CategoryRes.builder()
                    .name(theme.getName())
                    .type("theme")
                    .productCount((long) matched.size())
                    .imageUrl(imageUrl)
                    .displayOrder(theme.getDisplayOrder())
                    .hotCount(hotCount)
                    .build());
        }

        // 歷史資料兜底：字典尚未收錄但商品已存在時，先補顯示，避免前台突然消失。
        for (Map.Entry<String, List<Lottery>> entry : grouped.entrySet()) {
            if (!isNotBlank(entry.getKey())) {
                continue;
            }
            if (condition != null && isNotBlank(condition.getTheme())
                    && !entry.getKey().equals(resolveCanonicalThemeName(condition.getTheme()))) {
                continue;
            }
            if (condition != null && isNotBlank(condition.getKeyword())
                    && !matchesKeyword(entry.getKey(), condition.getKeyword())) {
                continue;
            }

            boolean exists = result.stream().anyMatch(r -> entry.getKey().equals(r.getName()));
            if (!exists) {
                CategoryRes fallback = buildCategoryRes(entry.getKey(), "theme", entry.getValue());
                fallback.setDisplayOrder(9999);
                result.add(fallback);
            }
        }

        result = result.stream()
                .sorted(Comparator.comparing(CategoryRes::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CategoryRes::getHotCount, Comparator.nullsFirst(Comparator.reverseOrder()))
                        .thenComparing(CategoryRes::getName))
                .collect(Collectors.toList());
        
        log.info("✅ 查詢完成，共 {} 個主題", result.size());
        return result;
    }
    
    @Override
    public List<CategoryRes> queryTags(QueryReq<CategoryCondition> req) {
        log.info("🏷️ 查詢商品標籤");

        CategoryCondition condition = req != null ? req.getCondition() : null;

        LotteryExample example = buildExample(condition);
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);

        String tagStatus = resolveRequestedStatus(condition != null ? condition.getStatus() : null, true);
        List<LotteryTag> tags = lotteryTagMapper.selectAll(tagStatus);
        Map<String, Long> tagCounts = new HashMap<>();
        Map<String, String> tagImages = new HashMap<>();
        Map<String, Long> tagHotCounts = new HashMap<>();

        for (Lottery lottery : lotteries) {
            for (String tagName : parseLotteryTags(lottery.getTags())) {
                String trimmedTag = normalizeName(tagName);
                if (!trimmedTag.isEmpty()) {
                    tagCounts.put(trimmedTag, tagCounts.getOrDefault(trimmedTag, 0L) + 1);

                    if (!tagImages.containsKey(trimmedTag)) {
                        tagImages.put(trimmedTag, lottery.getImageUrl());
                    }

                    Integer hotCount = lottery.getHotCount() != null ? lottery.getHotCount() : 0;
                    tagHotCounts.put(trimmedTag, tagHotCounts.getOrDefault(trimmedTag, 0L) + hotCount);
                }
            }
        }

        List<CategoryRes> result = tags.stream()
                .filter(tag -> condition == null || !isNotBlank(condition.getTags()) || tag.getName().equals(condition.getTags()))
                .filter(tag -> condition == null || !isNotBlank(condition.getKeyword()) || matchesKeyword(tag.getName(), condition.getKeyword()))
                .map(tag -> CategoryRes.builder()
                        .name(tag.getName())
                        .type("tag")
                        .productCount(tagCounts.getOrDefault(tag.getName(), 0L))
                        .imageUrl(tagImages.get(tag.getName()))
                        .displayOrder(tag.getDisplayOrder())
                        .hotCount(tagHotCounts.getOrDefault(tag.getName(), 0L))
                        .build())
                .collect(Collectors.toList());

        // 歷史資料兜底：若商品上有舊標籤，但尚未進字典，也暫時保留可見
            final List<CategoryRes> existingResult = result;
        List<CategoryRes> historical = tagCounts.entrySet().stream()
                .filter(entry -> condition == null || !isNotBlank(condition.getTags()) || entry.getKey().equals(condition.getTags()))
                .filter(entry -> condition == null || !isNotBlank(condition.getKeyword()) || matchesKeyword(entry.getKey(), condition.getKeyword()))
                .filter(entry -> existingResult.stream().noneMatch(r -> entry.getKey().equals(r.getName())))
                .map(entry -> CategoryRes.builder()
                        .name(entry.getKey())
                        .type("tag")
                        .productCount(entry.getValue())
                        .imageUrl(tagImages.get(entry.getKey()))
                        .displayOrder(9999)
                        .hotCount(tagHotCounts.getOrDefault(entry.getKey(), 0L))
                        .build())
                .collect(Collectors.toList());

        result.addAll(historical);
        result = result.stream()
                .sorted(Comparator.comparing(CategoryRes::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CategoryRes::getProductCount, Comparator.nullsFirst(Comparator.reverseOrder()))
                        .thenComparing(CategoryRes::getName))
                .collect(Collectors.toList());
        
        log.info("✅ 查詢完成，共 {} 個標籤", result.size());
        return result;
    }
    
    @Override
    public List<CategoryRes> getHotThemes(int limit) {
        log.info("🔥 查詢熱門主題，限制 {} 個", limit);

        LotteryExample example = new LotteryExample();
        example.createCriteria().andStatusEqualTo("ON_SHELF");
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);

        Map<String, List<Lottery>> grouped = lotteries.stream()
                .filter(l -> isNotBlank(l.getTheme()))
            .collect(Collectors.groupingBy(l -> resolveCanonicalThemeName(l.getTheme())));

        Map<String, LotteryTheme> themeMap = lotteryThemeMapper.selectAll("ACTIVE").stream()
                .collect(Collectors.toMap(LotteryTheme::getName, t -> t, (a, b) -> a));

        List<CategoryRes> result = grouped.entrySet().stream()
                .map(entry -> {
                    CategoryRes res = buildCategoryRes(entry.getKey(), "theme", entry.getValue());
                    LotteryTheme theme = themeMap.get(entry.getKey());
                    if (theme != null) {
                        res.setDisplayOrder(theme.getDisplayOrder());
                        if (isNotBlank(theme.getImageUrl())) {
                            res.setImageUrl(theme.getImageUrl());
                        }
                    }
                    return res;
                })
                .sorted(Comparator.comparing(CategoryRes::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(CategoryRes::getHotCount, Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());
        
        log.info("✅ 查詢完成，返回 {} 個熱門主題", result.size());
        return result;
    }

    @Override
    public List<CategoryRes> suggestThemes(String keyword, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        String normalizedKeyword = normalizeName(keyword);

        List<LotteryTheme> list;
        if (isNotBlank(normalizedKeyword)) {
            list = lotteryThemeMapper.suggestByKeyword(normalizedKeyword, safeLimit);
        } else {
            list = lotteryThemeMapper.selectAll("ACTIVE").stream().limit(safeLimit).collect(Collectors.toList());
        }

        // 別名命中時，將 canonical 主題補進建議，避免店家再建重複。
        String normalizedKey = normalizeKey(normalizedKeyword);
        if (isNotBlank(normalizedKey)) {
            LotteryThemeAlias alias = lotteryThemeAliasMapper.selectByNormalizedName(normalizedKey);
            if (alias != null) {
                LotteryTheme canonical = lotteryThemeMapper.selectById(alias.getThemeId());
                if (canonical != null && "ACTIVE".equals(canonical.getStatus())
                        && list.stream().noneMatch(t -> t.getId().equals(canonical.getId()))) {
                    list.add(0, canonical);
                }
            }
        }

        return list.stream().map(t -> CategoryRes.builder()
                .name(t.getName())
                .type("theme")
                .productCount(0L)
                .imageUrl(t.getImageUrl())
                .displayOrder(t.getDisplayOrder())
                .hotCount(0L)
                .build()).collect(Collectors.toList());
    }

    @Override
    public CategoryRes upsertTheme(String name, String imageUrl, Integer displayOrder) {
        String normalizedName = normalizeName(name);
        if (!isNotBlank(normalizedName)) {
            throw new BusinessException("主題名稱不可為空");
        }

        // 若輸入命中既有 alias，直接回 canonical，避免重複新增。
        String normalizedKey = normalizeKey(normalizedName);
        LotteryThemeAlias hitAlias = lotteryThemeAliasMapper.selectByNormalizedName(normalizedKey);
        if (hitAlias != null) {
            LotteryTheme canonical = lotteryThemeMapper.selectById(hitAlias.getThemeId());
            if (canonical != null && "ACTIVE".equals(canonical.getStatus())) {
                return buildThemeRes(canonical, countThemeProducts(canonical.getName()), countThemeHot(canonical.getName()));
            }
        }

        LotteryTheme existed = lotteryThemeMapper.selectByNormalizedName(normalizedKey);
        if (existed != null) {
            if (!"ACTIVE".equals(existed.getStatus())) {
                existed.setStatus("ACTIVE");
            }
            boolean needUpdate = false;
            if (isNotBlank(imageUrl) && !imageUrl.equals(existed.getImageUrl())) {
                existed.setImageUrl(imageUrl.trim());
                needUpdate = true;
            }
            if (displayOrder != null) {
                existed.setDisplayOrder(displayOrder);
                needUpdate = true;
            }
            if (!"ACTIVE".equals(existed.getStatus())) {
                needUpdate = true;
            }
            if (needUpdate) {
                lotteryThemeMapper.updateByPrimaryKeySelective(existed);
            }
            return buildThemeRes(existed, countThemeProducts(existed.getName()), countThemeHot(existed.getName()));
        }

        LotteryTheme theme = new LotteryTheme();
        theme.setId(UUID.randomUUID().toString());
        theme.setName(normalizedName);
        theme.setImageUrl(isNotBlank(imageUrl) ? imageUrl.trim() : null);
        theme.setStatus("ACTIVE");
        theme.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        theme.setCreatedAt(java.time.LocalDateTime.now());
        theme.setUpdatedAt(java.time.LocalDateTime.now());
        lotteryThemeMapper.insert(theme);

        // 建立 canonical 的自我別名，後續 fuzzy/canonical 解析更穩定。
        createOrRestoreAlias(theme.getId(), theme.getName());

        return buildThemeRes(theme, 0L, 0L);
    }

    @Override
    public CategoryRes updateTheme(String id, String name, String imageUrl, Integer displayOrder) {
        LotteryTheme theme = lotteryThemeMapper.selectById(id);
        if (theme == null) {
            throw new BusinessException("主題不存在");
        }

        if (isNotBlank(name)) {
            String normalizedName = normalizeName(name);
            LotteryTheme duplicate = lotteryThemeMapper.selectByNormalizedName(normalizeKey(normalizedName));
            if (duplicate != null && !duplicate.getId().equals(id)) {
                throw new BusinessException("主題名稱已存在：" + normalizedName);
            }

            if (!normalizedName.equals(theme.getName()) && countThemeProducts(theme.getName()) > 0) {
                throw new BusinessException("主題已有商品使用，僅可修改圖片與排序，不可改名");
            }
            theme.setName(normalizedName);
        }
        if (imageUrl != null) {
            theme.setImageUrl(isNotBlank(imageUrl) ? imageUrl.trim() : null);
        }
        if (displayOrder != null) {
            theme.setDisplayOrder(displayOrder);
        }

        lotteryThemeMapper.updateByPrimaryKeySelective(theme);
        createOrRestoreAlias(theme.getId(), theme.getName());
        return buildThemeRes(theme, countThemeProducts(theme.getName()), countThemeHot(theme.getName()));
    }

    @Override
    public void deleteTheme(String id) {
        LotteryTheme theme = lotteryThemeMapper.selectById(id);
        if (theme == null) {
            throw new BusinessException("主題不存在");
        }

        long usedCount = countThemeProducts(theme.getName());
        if (usedCount > 0) {
            throw new BusinessException("主題已被商品使用，無法刪除");
        }

        lotteryThemeMapper.softDeleteById(id);
        List<LotteryThemeAlias> aliases = lotteryThemeAliasMapper.selectByThemeId(id, "ACTIVE");
        for (LotteryThemeAlias alias : aliases) {
            lotteryThemeAliasMapper.softDeleteById(alias.getId());
        }
    }

    @Override
    public CategoryRes createTag(String name, Integer displayOrder, String status) {
        String normalizedName = normalizeName(name);
        if (!isNotBlank(normalizedName)) {
            throw new BusinessException("標籤名稱不可為空");
        }

        LotteryTag existed = lotteryTagMapper.selectByNormalizedName(normalizeKey(normalizedName));
        if (existed != null) {
            throw new BusinessException("標籤名稱已存在：" + normalizedName);
        }

        LotteryTag tag = new LotteryTag();
        tag.setId(UUID.randomUUID().toString());
        tag.setName(normalizedName);
        tag.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        tag.setStatus(normalizeStatus(status));
        tag.setCreatedAt(java.time.LocalDateTime.now());
        tag.setUpdatedAt(java.time.LocalDateTime.now());

        lotteryTagMapper.insert(tag);
        return buildTagRes(tag, 0L, 0L, null);
    }

    @Override
    public CategoryRes updateTag(String id, String name, Integer displayOrder, String status) {
        LotteryTag tag = lotteryTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("標籤不存在");
        }

        if (isNotBlank(name)) {
            String normalizedName = normalizeName(name);
            LotteryTag duplicate = lotteryTagMapper.selectByNormalizedName(normalizeKey(normalizedName));
            if (duplicate != null && !duplicate.getId().equals(id)) {
                throw new BusinessException("標籤名稱已存在：" + normalizedName);
            }
            if (!normalizedName.equals(tag.getName()) && countTagProducts(tag.getName()) > 0) {
                throw new BusinessException("標籤已有商品使用，不能直接改名");
            }
            tag.setName(normalizedName);
        }
        if (displayOrder != null) {
            tag.setDisplayOrder(displayOrder);
        }
        if (status != null) {
            tag.setStatus(normalizeStatus(status));
        }

        lotteryTagMapper.updateByPrimaryKeySelective(tag);
        return buildTagRes(tag, countTagProducts(tag.getName()), countTagHot(tag.getName()), null);
    }

    @Override
    public void deleteTag(String id) {
        LotteryTag tag = lotteryTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("標籤不存在");
        }

        long usedCount = countTagProducts(tag.getName());
        if (usedCount > 0) {
            throw new BusinessException("標籤已被商品使用，無法刪除");
        }

        lotteryTagMapper.deleteById(id);
    }

    @Override
    public void validateTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        Set<String> allowed = lotteryTagMapper.selectAll("ACTIVE").stream()
                .map(LotteryTag::getName)
                .map(this::normalizeName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<String> invalid = tagNames.stream()
                .filter(this::isNotBlank)
                .map(this::normalizeName)
                .filter(tag -> !allowed.contains(tag.toLowerCase()))
                .distinct()
                .collect(Collectors.toList());

        if (!invalid.isEmpty()) {
            throw new BusinessException("標籤不合法，請從後台標籤清單選擇：" + String.join("、", invalid));
        }
    }

    @Override
    public String resolveCanonicalThemeName(String inputThemeName) {
        if (!isNotBlank(inputThemeName)) {
            return null;
        }

        String normalizedName = normalizeName(inputThemeName);
        String normalizedKey = normalizeKey(normalizedName);

        LotteryThemeAlias alias = lotteryThemeAliasMapper.selectByNormalizedName(normalizedKey);
        if (alias != null) {
            LotteryTheme canonical = lotteryThemeMapper.selectById(alias.getThemeId());
            if (canonical != null && "ACTIVE".equals(canonical.getStatus())) {
                return canonical.getName();
            }
        }

        LotteryTheme theme = lotteryThemeMapper.selectByNormalizedName(normalizedKey);
        if (theme != null) {
            return theme.getName();
        }

        return normalizedName;
    }

    @Override
    public CategoryRes createThemeAlias(String themeId, String aliasName) {
        LotteryTheme theme = lotteryThemeMapper.selectById(themeId);
        if (theme == null || !"ACTIVE".equals(theme.getStatus())) {
            throw new BusinessException("主題不存在或未啟用");
        }

        createOrRestoreAlias(themeId, aliasName);
        return buildThemeRes(theme, countThemeProducts(theme.getName()), countThemeHot(theme.getName()));
    }

    @Override
    public void deleteThemeAlias(String aliasId) {
        LotteryThemeAlias alias = lotteryThemeAliasMapper.selectById(aliasId);
        if (alias == null) {
            throw new BusinessException("同義詞不存在");
        }
        lotteryThemeAliasMapper.softDeleteById(aliasId);
    }

    @Override
    public CategoryHealthRes getCategoryHealth() {
        List<LotteryTheme> activeThemes = lotteryThemeMapper.selectAll("ACTIVE");
        List<LotteryTheme> inactiveThemes = lotteryThemeMapper.selectAll("INACTIVE");
        List<LotteryTag> activeTags = lotteryTagMapper.selectAll("ACTIVE");
        List<LotteryTag> inactiveTags = lotteryTagMapper.selectAll("INACTIVE");

        Set<String> activeThemeNames = activeThemes.stream().map(LotteryTheme::getName).collect(Collectors.toSet());
        Set<String> activeTagNames = activeTags.stream().map(LotteryTag::getName).collect(Collectors.toSet());

        List<Lottery> allLotteries = lotteryMapper.selectByExample(new LotteryExample());

        long missingThemeCount = allLotteries.stream()
                .filter(l -> isNotBlank(l.getTheme()))
                .map(l -> resolveCanonicalThemeName(l.getTheme()))
                .filter(this::isNotBlank)
                .filter(name -> !activeThemeNames.contains(name))
                .count();

        long invalidTagCount = allLotteries.stream()
                .filter(l -> hasInvalidTag(l.getTags(), activeTagNames))
                .count();

        List<String> duplicateThemeCandidates = activeThemes.stream()
                .collect(Collectors.groupingBy(t -> normalizeKey(t.getName())))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> e.getValue().stream().map(LotteryTheme::getName).sorted().collect(Collectors.joining(" | ")))
                .sorted()
                .collect(Collectors.toList());

        return CategoryHealthRes.builder()
                .activeThemeCount(activeThemes.size())
                .inactiveThemeCount(inactiveThemes.size())
                .activeTagCount(activeTags.size())
                .inactiveTagCount(inactiveTags.size())
                .lotteriesWithoutThemeInDictionary(missingThemeCount)
                .lotteriesWithInvalidTags(invalidTagCount)
                .duplicateThemeCandidates(duplicateThemeCandidates)
                .build();
    }
    
    // ==================== 私有輔助方法 ====================
    
    /**
     * 建立查詢條件
     */
    private LotteryExample buildExample(CategoryCondition condition) {
        LotteryExample example = new LotteryExample();
        LotteryExample.Criteria criteria = example.createCriteria();
        
        if (condition != null) {
            // 狀態篩選
            if (condition.getStatus() != null && !condition.getStatus().isEmpty()) {
                criteria.andStatusEqualTo(condition.getStatus());
            } else {
                // 預設只查上架商品
                criteria.andStatusEqualTo("ON_SHELF");
            }
            
            // 類別篩選
            if (condition.getCategory() != null && !condition.getCategory().isEmpty()) {
                criteria.andCategoryEqualTo(condition.getCategory());
            }
            
            // 主題篩選
            if (condition.getTheme() != null && !condition.getTheme().isEmpty()) {
                criteria.andThemeEqualTo(resolveCanonicalThemeName(condition.getTheme()));
            }
            
            // 標籤篩選（模糊匹配）
            if (condition.getTags() != null && !condition.getTags().isEmpty()) {
                criteria.andTagsLike("%" + condition.getTags() + "%");
            }
            
            // 關鍵字搜尋（搜尋主題或標籤）
            if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
                LotteryExample.Criteria keywordCriteria = example.createCriteria();
                keywordCriteria.andThemeLike("%" + normalizeName(condition.getKeyword()) + "%");
                
                LotteryExample.Criteria tagCriteria = example.createCriteria();
                tagCriteria.andTagsLike("%" + normalizeName(condition.getKeyword()) + "%");
                
                example.or(keywordCriteria);
                example.or(tagCriteria);
            }
        } else {
            // 無條件時預設只查上架商品
            criteria.andStatusEqualTo("ON_SHELF");
        }
        
        return example;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('　', ' ').trim().replaceAll("\\s+", " ");
    }

    private String normalizeKey(String value) {
        return normalizeName(value).toLowerCase(Locale.ROOT);
    }

    private String resolveRequestedStatus(String requestedStatus, boolean defaultActive) {
        if (!isNotBlank(requestedStatus)) {
            return defaultActive ? "ACTIVE" : null;
        }
        String normalized = requestedStatus.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
            return defaultActive ? "ACTIVE" : null;
        }
        return normalized;
    }

    private boolean matchesKeyword(String source, String keyword) {
        if (!isNotBlank(keyword)) {
            return true;
        }
        return normalizeName(source).contains(normalizeName(keyword));
    }

    private String normalizeStatus(String status) {
        if (!isNotBlank(status)) {
            return "ACTIVE";
        }
        String normalized = status.trim().toUpperCase();
        if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
            throw new BusinessException("status 僅允許 ACTIVE/INACTIVE");
        }
        return normalized;
    }

    private String resolveDisplayCategoryKey(Lottery lottery) {
        if (lottery == null) {
            return "";
        }

        String category = normalizeUpper(lottery.getCategory());
        if (!isNotBlank(category)) {
            return "";
        }

        if (MODE_SCRATCH.equals(normalizeUpper(lottery.getSubCategory()))
                || MODE_SCRATCH.equals(normalizeUpper(lottery.getPlayMode()))) {
            return MODE_SCRATCH;
        }

        if (CATEGORY_OFFICIAL_ICHIBAN.equals(category)
                || CATEGORY_GACHA.equals(category)
                || CATEGORY_TRADING_CARD.equals(category)
                || CATEGORY_CUSTOM_GACHA.equals(category)) {
            return category;
        }

        return "";
    }

    private CategoryRes toDisplayCategoryRes(DisplayCategoryBucket bucket, List<Lottery> lotteries) {
        String imageUrl = lotteries.stream()
                .map(Lottery::getImageUrl)
                .filter(this::isNotBlank)
                .findFirst()
                .orElse(null);

        long totalHotCount = lotteries.stream()
                .mapToLong(lottery -> lottery.getHotCount() != null ? lottery.getHotCount() : 0)
                .sum();

        return CategoryRes.builder()
                .name(bucket.name())
                .type("display-category")
                .productCount((long) lotteries.size())
                .imageUrl(imageUrl)
                .displayOrder(bucket.displayOrder())
                .hotCount(totalHotCount)
                .build();
    }

    private String normalizeUpper(String value) {
        return isNotBlank(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
    }

    private List<String> parseLotteryTags(String rawTags) {
        if (!isNotBlank(rawTags)) {
            return List.of();
        }

        String trimmed = rawTags.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                List<String> jsonTags = objectMapper.readValue(trimmed, new TypeReference<List<String>>() {});
                return jsonTags.stream().filter(this::isNotBlank).map(this::normalizeName).collect(Collectors.toList());
            } catch (JsonProcessingException e) {
                log.debug("標籤 JSON 解析失敗，改用逗號切分: {}", trimmed);
            }
        }

        return Arrays.stream(trimmed.split(","))
                .map(this::normalizeName)
                .filter(this::isNotBlank)
                .collect(Collectors.toList());
    }

    private long countThemeProducts(String themeName) {
        LotteryExample example = new LotteryExample();
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);
        return lotteries.stream()
                .filter(l -> themeName.equals(resolveCanonicalThemeName(l.getTheme())))
                .count();
    }

    private long countThemeHot(String themeName) {
        LotteryExample example = new LotteryExample();
        example.createCriteria().andThemeEqualTo(themeName);
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);
        return lotteries.stream()
            .filter(l -> themeName.equals(resolveCanonicalThemeName(l.getTheme())))
            .mapToLong(l -> l.getHotCount() != null ? l.getHotCount() : 0)
            .sum();
    }

    private long countTagProducts(String tagName) {
        return lotteryMapper.selectByExample(new LotteryExample()).stream()
            .filter(lottery -> parseLotteryTags(lottery.getTags()).stream()
                .map(this::normalizeName)
                .anyMatch(tag -> tagName.equals(tag)))
                .count();
    }

    private long countTagHot(String tagName) {
        return lotteryMapper.selectByExample(new LotteryExample()).stream()
                .filter(lottery -> parseLotteryTags(lottery.getTags()).stream()
                        .map(this::normalizeName)
                        .anyMatch(tag -> tagName.equals(tag)))
                .mapToLong(lottery -> lottery.getHotCount() != null ? lottery.getHotCount() : 0)
                .sum();
    }

    private void createOrRestoreAlias(String themeId, String aliasName) {
        String normalizedAlias = normalizeName(aliasName);
        if (!isNotBlank(normalizedAlias)) {
            return;
        }

        String key = normalizeKey(normalizedAlias);

        LotteryTheme existingTheme = lotteryThemeMapper.selectByNormalizedName(key);
        if (existingTheme != null && !existingTheme.getId().equals(themeId)) {
            throw new BusinessException("同義詞與其他主題名稱衝突：" + normalizedAlias);
        }

        LotteryThemeAlias existing = lotteryThemeAliasMapper.selectByNormalizedName(key);
        if (existing != null) {
            if (!existing.getThemeId().equals(themeId)) {
                throw new BusinessException("同義詞已被其他主題使用：" + normalizedAlias);
            }
            if (!"ACTIVE".equals(existing.getStatus())
                    || !normalizedAlias.equals(existing.getAliasName())) {
                existing.setAliasName(normalizedAlias);
                existing.setStatus("ACTIVE");
                existing.setNormalizedName(key);
                lotteryThemeAliasMapper.updateByPrimaryKeySelective(existing);
            }
            return;
        }

        LotteryThemeAlias alias = new LotteryThemeAlias();
        alias.setId(UUID.randomUUID().toString());
        alias.setThemeId(themeId);
        alias.setAliasName(normalizedAlias);
        alias.setNormalizedName(key);
        alias.setStatus("ACTIVE");
        alias.setCreatedAt(java.time.LocalDateTime.now());
        alias.setUpdatedAt(java.time.LocalDateTime.now());
        lotteryThemeAliasMapper.insert(alias);
    }

    private boolean hasInvalidTag(String rawTags, Set<String> activeTagNames) {
        List<String> tags = parseLotteryTags(rawTags);
        if (tags.isEmpty()) {
            return false;
        }
        return tags.stream().map(this::normalizeName).anyMatch(tag -> !activeTagNames.contains(tag));
    }

    private CategoryRes buildThemeRes(LotteryTheme theme, long productCount, long hotCount) {
        return CategoryRes.builder()
                .name(theme.getName())
                .type("theme")
                .productCount(productCount)
                .imageUrl(theme.getImageUrl())
                .displayOrder(theme.getDisplayOrder())
                .hotCount(hotCount)
                .build();
    }

    private CategoryRes buildTagRes(LotteryTag tag, long productCount, long hotCount, String imageUrl) {
        return CategoryRes.builder()
                .name(tag.getName())
                .type("tag")
                .productCount(productCount)
                .imageUrl(imageUrl)
                .displayOrder(tag.getDisplayOrder())
                .hotCount(hotCount)
                .build();
    }
    
    /**
     * 建立 CategoryRes
     */
    private CategoryRes buildCategoryRes(String name, String type, List<Lottery> lotteries) {
        // 計算商品數量
        long count = lotteries.size();
        
        // 取得代表圖片（第一個商品的圖片）
        String imageUrl = !lotteries.isEmpty() ? lotteries.get(0).getImageUrl() : null;
        
        // 計算總熱度
        long totalHotCount = lotteries.stream()
                .mapToLong(l -> l.getHotCount() != null ? l.getHotCount() : 0)
                .sum();
        
        return CategoryRes.builder()
                .name(name)
                .type(type)
                .productCount(count)
                .imageUrl(imageUrl)
                .hotCount(totalHotCount)
                .build();
    }

    private record DisplayCategoryBucket(String key, String name, Integer displayOrder) {
    }
}
