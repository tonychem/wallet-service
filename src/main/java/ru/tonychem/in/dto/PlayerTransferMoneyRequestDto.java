package ru.tonychem.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerTransferMoneyRequestDto {
    @NotBlank
    private String recipient;
    @NotNull
    private Double amount;
}
