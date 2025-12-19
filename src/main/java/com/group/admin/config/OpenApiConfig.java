package com.group.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI 3.0 配置
 * 
 * <p>提供 API 文件自動生成功能</p>
 * <p>存取路徑：http://localhost:8080/swagger-ui/index.html</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * OpenAPI 配置
     * 
     * @return OpenAPI 物件
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(apiInfo())
                .servers(getServers())
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("請輸入 JWT Token（不需要加 Bearer 前綴）")
                        )
                );
    }

    /**
     * API 基本資訊
     */
    private Info apiInfo() {
        return new Info()
                .title("KUJI 抽獎平台 API")
                .description("""
                        ## 概述
                        KUJI 抽獎平台後端 API 文件
                        
                        ## 認證方式
                        - 後台 API (`/admin/**`)：需要 Admin/StoreOwner/StoreEditor 角色
                        - 前台 API (`/api/**`)：需要 USER 角色
                        
                        ## 角色說明
                        | 角色 | 說明 |
                        |------|------|
                        | Admin | 平台最高管理者，擁有所有權限 |
                        | StoreOwner | 店家主帳號，可管理自己店家的商品、訂單、報表 |
                        | StoreEditor | 店家編輯者，僅可操作商品與部分訂單功能 |
                        | USER | 前台玩家 |
                        
                        ## 錯誤碼
                        所有錯誤回應都會包含 `error` 物件，包括 `code` 和 `message`
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("KUJI Team")
                        .email("support@kuji.com")
                )
                .license(new License()
                        .name("Private")
                        .url("https://kuji.com")
                );
    }

    /**
     * 伺服器列表
     */
    private List<Server> getServers() {
        if ("prod".equals(activeProfile)) {
            return List.of(
                    new Server().url("https://api.kuji.com").description("正式環境")
            );
        }
        return List.of(
                new Server().url("http://localhost:8080").description("開發環境")
        );
    }
}
