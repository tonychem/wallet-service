package ru.yandex.metrics.performance;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class PerformanceConfig {
    @Bean("PerformanceAspect")
    public PerformanceAspect performanceAspect() {
        return new PerformanceAspect();
    }
}
