package ru.yandex.wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.wallet.domain.dto.AuthenticatedPlayerDto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class BeanConfiguration {
    @Bean
    public MessageDigest messageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5");
    }

    @Bean
    public Map<UUID,AuthenticatedPlayerDto> authentications() {
        return new HashMap<>();
    }
}
