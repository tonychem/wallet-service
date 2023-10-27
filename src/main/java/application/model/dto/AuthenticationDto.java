package application.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthenticationDto {
    private Long id;
    private String login;
    private String username;
    @JsonIgnore
    private UUID sessionId;
    private BigDecimal balance;
}
