package in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionsDto {
    private Collection<String> ids;
}
