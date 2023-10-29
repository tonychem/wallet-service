package ru.tonychem.in.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnsecuredAuthenticationRequestDto {
    @NotBlank
    private String login;
    @NotBlank
    private String password;
}
