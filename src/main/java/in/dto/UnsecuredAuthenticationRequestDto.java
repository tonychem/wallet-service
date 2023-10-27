package in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import validation.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnsecuredAuthenticationRequestDto {
    @NotBlank
    private String login;
    @NotBlank
    private String password;
}
