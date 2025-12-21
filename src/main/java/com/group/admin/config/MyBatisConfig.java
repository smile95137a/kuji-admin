package com.group.admin.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 配置類
 * 
 * 目的：
 * 1. 精確控制 Mapper XML 載入路徑，避免重複載入
 * 2. 確保 SqlSessionFactory 只初始化一次
 * 3. 防止 DevTools 或其他機制導致的重複註冊
 * 
 * 注意：
 * - 此配置會覆蓋 application.yml 中的 mybatis.mapper-locations
 * - @MapperScan 已在 AdminApplication 中定義，這裡不重複
 */
@Configuration
public class MyBatisConfig {

    /**
     * 自定義 SqlSessionFactory
     * 使用精確的資源載入路徑，避免 classpath* 的多重掃描
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // 設定 Type Aliases（實體類掃描路徑）
        sessionFactory.setTypeAliasesPackage("com.group.admin.entity");
        
        // 使用 PathMatchingResourcePatternResolver 精確載入 Mapper XML
        // classpath: 只會掃描當前 classpath 根目錄
        // classpath*: 會掃描所有 jar 包（可能導致重複）
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(
            resolver.getResources("classpath:/mapper/*.xml")
        );
        
        // MyBatis Configuration 設定
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLazyLoadingEnabled(false);
        configuration.setAggressiveLazyLoading(false);
        
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }
}
