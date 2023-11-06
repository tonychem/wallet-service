package ru.yandex.wallet.in.controller;

import ru.yandex.wallet.in.UnpackedJwtClaims;
import ru.yandex.wallet.util.JwtUtils;

import java.util.UUID;

public abstract class AbstractTokenConsumer {
    protected UnpackedJwtClaims unpackJwtClaims(String authHeader, String secret) {
        String jwt = authHeader.substring(7);

        Long id = Long.valueOf((Integer) JwtUtils.extractClaim(jwt, claims -> claims.get("id"), secret));
        String login = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("login"), secret);
        String username = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("username"), secret);
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id"), secret));

        return new UnpackedJwtClaims(id, login, username, sessionId);
    }
}
