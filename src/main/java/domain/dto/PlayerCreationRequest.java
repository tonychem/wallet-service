package domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import validation.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayerCreationRequest {
    @NotBlank
    private String login;
    private byte[] password;
    private String username;
}
