package ru.yandex.wallet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class ApplicationConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public FilterRegistrationBean<JwtTokenFilter> loggingFilter() {
        FilterRegistrationBean<JwtTokenFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new JwtTokenFilter(objectMapper()));
        registrationBean.addUrlPatterns("/logout", "/player-management/*");

        return registrationBean;
    }
}
