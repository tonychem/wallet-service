package ru.yandex.wallet.in.controller;

import model.dto.out.AuthenticatedPlayerDto;
import ru.yandex.wallet.util.JwtUtils;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractTokenProducer {
    protected String generateJwt(AuthenticatedPlayerDto authentication, UUID sessionId, String secret) {
        Map<String, Object> claims = Map.of(
                "login", authentication.getLogin(),
                "id", authentication.getId(),
                "username", authentication.getUsername(),
                "session-id", sessionId
        );

        String token = "Bearer " + JwtUtils.generateToken(secret, claims);

        return token;
    }
}
