package ru.tonychem.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayerRequestMoneyDto {
    @NotBlank
    private String donor;

    @NotNull
    private Double amount;
}
