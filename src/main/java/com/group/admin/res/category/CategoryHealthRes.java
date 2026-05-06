package com.group.admin.res.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryHealthRes {
    private long activeThemeCount;
    private long inactiveThemeCount;
    private long activeTagCount;
    private long inactiveTagCount;
    private long lotteriesWithoutThemeInDictionary;
    private long lotteriesWithInvalidTags;
    private List<String> duplicateThemeCandidates;
}
