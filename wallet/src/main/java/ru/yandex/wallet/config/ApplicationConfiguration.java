package ru.yandex.wallet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class ApplicationConfiguration {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public FilterRegistrationBean<JwtTokenFilter> loggingFilter() {
        FilterRegistrationBean<JwtTokenFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new JwtTokenFilter(objectMapper(), secret));
        registrationBean.addUrlPatterns("/logout", "/player-management/*");

        return registrationBean;
    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Wallet service")
                        .description("API documentation"));
    }
}
