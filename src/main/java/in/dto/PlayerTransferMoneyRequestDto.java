package in.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerTransferMoneyRequestDto {
    @NotEmpty
    private String recipient;
    @NotNull
    private Double amount;
}
