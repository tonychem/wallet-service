package ru.tonychem.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayerCreationRequest {
    private String login;
    private byte[] password;
    private String username;
}
