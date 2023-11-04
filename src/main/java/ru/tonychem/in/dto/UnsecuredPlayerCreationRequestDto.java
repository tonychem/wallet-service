package ru.tonychem.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tonychem.aop.annotations.validation.NotBlank;
import ru.tonychem.aop.annotations.validation.Validated;


/**
 * Входящий пользовательский запрос на регистрацию нового пользователя с незашифрованным паролем
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class UnsecuredPlayerCreationRequestDto {
    @NotBlank
    private String login;
    @NotBlank
    private String password;
    @NotBlank
    private String username;
}
