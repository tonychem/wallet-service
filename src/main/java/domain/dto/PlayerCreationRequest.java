package domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerCreationRequest {
    private String login;
    private byte[] password;
    private String username;
}
