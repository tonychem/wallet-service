package controller;

import lombok.Value;

import java.util.UUID;

/**
 * Объект сессии, содержащей информацию о текущем авторизованном пользователе.
 */
@Value
public class Session {
    Long userId;
    String login;
    String username;
    UUID sessionId;
}
