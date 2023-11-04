package ru.tonychem.in.controller;

import ru.tonychem.domain.dto.AuthenticatedPlayerDto;
import ru.tonychem.util.JwtUtils;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractTokenProducer {
    protected String generateJwt(AuthenticatedPlayerDto authentication, UUID sessionId) {
        Map<String, Object> claims = Map.of(
                "login", authentication.getLogin(),
                "id", authentication.getId(),
                "username", authentication.getUsername(),
                "session-id", sessionId
        );

        String token = "Bearer " + JwtUtils.generateToken(claims);

        return token;
    }
}
