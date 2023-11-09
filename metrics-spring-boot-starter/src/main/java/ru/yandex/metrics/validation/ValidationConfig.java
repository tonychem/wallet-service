package ru.yandex.metrics.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class ValidationConfig {
    @Bean("ValidationAspect")
    public ValidationAspect validationAspect() {
        return new ValidationAspect();
    }
}
