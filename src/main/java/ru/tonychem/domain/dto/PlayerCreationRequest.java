package ru.tonychem.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Информация о новом пользователе, содержащая пароль в зашифрованном виде
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayerCreationRequest {
    private String login;
    private byte[] password;
    private String username;
}
