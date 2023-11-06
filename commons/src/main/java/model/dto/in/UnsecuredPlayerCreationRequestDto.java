package model.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Входящий пользовательский запрос на регистрацию нового пользователя с незашифрованным паролем
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnsecuredPlayerCreationRequestDto {
    private String login;
    private String password;
    private String username;
}
