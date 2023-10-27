package in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import validation.NotEmpty;

import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionsListDto {
    @NotEmpty
    private Collection<String> ids;
}
