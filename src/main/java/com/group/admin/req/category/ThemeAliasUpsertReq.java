package com.group.admin.req.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ThemeAliasUpsertReq {

    @NotBlank(message = "同義詞名稱不可為空")
    @Size(max = 100, message = "同義詞名稱長度不可超過 100")
    @Schema(description = "主題同義詞名稱", example = "火影")
    private String aliasName;
}
