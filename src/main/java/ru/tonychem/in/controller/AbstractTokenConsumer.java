package ru.tonychem.in.controller;

import ru.tonychem.in.dto.UnpackedJwtClaims;
import ru.tonychem.util.JwtUtils;

import java.util.UUID;

public abstract class AbstractTokenConsumer {
    protected UnpackedJwtClaims unpackJwtClaims(String authHeader) {
        String jwt = authHeader.substring(7);

        Long id = Long.valueOf((Integer) JwtUtils.extractClaim(jwt, claims -> claims.get("id")));
        String login = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("login"));
        String username = (String) JwtUtils.extractClaim(jwt, claims -> claims.get("username"));
        UUID sessionId = UUID.fromString((String) JwtUtils.extractClaim(jwt, claims -> claims.get("session-id")));

        return new UnpackedJwtClaims(id, login, username, sessionId);
    }
}
