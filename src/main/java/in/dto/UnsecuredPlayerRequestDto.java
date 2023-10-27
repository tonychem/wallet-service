package in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import validation.NotBlank;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnsecuredPlayerRequestDto {
    @NotBlank
    private String login;
    @NotBlank
    private String password;
    @NotBlank
    private String username;
}
