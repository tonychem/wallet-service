package ru.tonychem.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tonychem.aop.annotations.validation.NotBlank;
import ru.tonychem.aop.annotations.validation.Validated;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class UnsecuredAuthenticationRequestDto {
    @NotBlank
    private String login;
    @NotBlank
    private String password;
}
