package model.dto.in;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * Пользовательский список id транзакций в строковом представлении для подтверждения/отклонения
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionsListDto {
    private Collection<String> ids;
}
