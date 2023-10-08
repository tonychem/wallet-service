package infrastructure;

import lombok.Value;

import java.util.UUID;

@Value
public class Session {
    Long userId;
    String login;
    String username;
    UUID sessionId;
}
