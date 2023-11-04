package ru.tonychem.in.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class UnpackedJwtClaims {
    Long userId;
    String login;
    String username;
    UUID sessionId;
}
