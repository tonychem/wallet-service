package application;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

@Value
@EqualsAndHashCode
public class Authentication {
    Long id;
    String login;
    String username;
    UUID sessionID;
}
