package ru.tonychem.in.controller;

import ru.tonychem.application.model.dto.AuthenticationDto;
import ru.tonychem.util.JwtUtils;

import java.util.Map;

public abstract class AbstractTokenProducer {
    protected String generateJwt(AuthenticationDto authentication) {
        Map<String,Object> claims = Map.of(
                "login", authentication.getLogin(),
                "id", authentication.getId(),
                "username", authentication.getUsername(),
                "session-id", authentication.getSessionId()
        );

        String token = "Bearer " + JwtUtils.generateToken(claims);

        return token;
    }
}
