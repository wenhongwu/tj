package com.tianji.promotion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author whw
 * @title: PromotionConfig
 * @projectName tjxt
 * @description: TODO
 * @date 2024/4/10 14:33
 */
@Slf4j
@Configuration
public class PromotionConfig {

    @Bean
    public Executor generateExchangeCodeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 1.核心线程池大小
        executor.setCorePoolSize(2);
        // 2.最大线程池大小
        executor.setMaxPoolSize(5);
        // 3.队列大小
        executor.setQueueCapacity(200);
        // 4.现成名称
        executor.setThreadNamePrefix("exchange-code-handler-");
        // 5.拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
