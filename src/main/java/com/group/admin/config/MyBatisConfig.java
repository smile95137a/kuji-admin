package com.group.admin.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 配置類 - 已停用
 * 
 * 原因：與 Spring Boot 自動配置衝突，導致 Mapper XML 重複載入
 * 
 * 解決方案：
 * 1. 移除 AdminApplication 中的 exclude = MybatisAutoConfiguration.class
 * 2. 讓 Spring Boot 自動配置 MyBatis
 * 3. 此配置類保留但不使用（已註解掉 @Configuration）
 * 
 * 如需自定義配置，請在 application.yml 中設定：
 * mybatis:
 *   mapper-locations: classpath:mapper/*.xml
 *   type-aliases-package: com.group.admin.entity
 *   configuration:
 *     map-underscore-to-camel-case: true
 */
// @Configuration  // 已停用，使用 Spring Boot 自動配置
public class MyBatisConfig {

    /**
     * 自定義 SqlSessionFactory - 已停用
     */
    // @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        sessionFactory.setTypeAliasesPackage("com.group.admin.entity");
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(
            resolver.getResources("classpath:/mapper/*.xml")
        );
        
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLazyLoadingEnabled(false);
        configuration.setAggressiveLazyLoading(false);
        
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }
}
