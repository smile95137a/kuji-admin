package com.group.admin.res.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品主題同義詞回應 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeAliasRes {

    private String id;

    private String aliasName;

    private String status;
}
