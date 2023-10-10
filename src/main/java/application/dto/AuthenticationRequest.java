package application.dto;

import lombok.Value;

@Value
public class AuthenticationRequest {
    String login;
    byte[] password;
}
