package com.group.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 非同步任務和排程配置
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    
    /**
     * 配置非同步執行器
     * 用於郵件發送、日誌記錄等非同步操作
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心執行緒數
        executor.setCorePoolSize(5);
        // 最大執行緒數
        executor.setMaxPoolSize(20);
        // 佇列容量
        executor.setQueueCapacity(200);
        // 執行緒前綴名
        executor.setThreadNamePrefix("async-task-");
        // 拒絕策略：由呼叫執行緒處理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任務完成後再關閉
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 關閉時等待時間（秒）
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("✅ 非同步任務執行器已配置: corePoolSize={}, maxPoolSize={}", 
                 executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
}
