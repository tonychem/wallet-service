package model.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Пользовательский запрос на получение денежных средств от другого пользователя
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlayerRequestMoneyDto {
    private String donor;
    private Double amount;
}
