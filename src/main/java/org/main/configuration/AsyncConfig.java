package org.main.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {
    
    @Bean(name = "requestHandler")
    public Executor requestHandler() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // Numero minimo di thread
        executor.setMaxPoolSize(20);   // Numero massimo di thread
        executor.setQueueCapacity(500); // Capacità della coda di task
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
    
}
