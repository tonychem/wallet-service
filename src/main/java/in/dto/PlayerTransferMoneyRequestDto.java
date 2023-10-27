package in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import validation.NotBlank;
import validation.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerTransferMoneyRequestDto {
    @NotBlank
    private String recipient;
    @NotNull
    private Double amount;
}
