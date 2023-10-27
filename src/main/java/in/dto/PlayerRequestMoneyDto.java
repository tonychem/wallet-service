package in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import validation.NotBlank;
import validation.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayerRequestMoneyDto {
    @NotBlank
    private String donor;

    @NotNull
    private Double amount;
}
