package ru.tonychem.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.tonychem.domain.dto.BalanceDto;
import ru.tonychem.domain.dto.MoneyTransferRequest;
import ru.tonychem.domain.dto.MoneyTransferResponse;
import ru.tonychem.in.dto.PlayerRequestMoneyDto;
import ru.tonychem.in.dto.PlayerTransferMoneyRequestDto;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MoneyTransferMapper {
    MoneyTransferMapper INSTANCE = Mappers.getMapper(MoneyTransferMapper.class);

    @Mapping(source = "moneyRequestDto.donor", target = "moneyFrom")
    @Mapping(source = "moneyRequestDto.amount", target = "amount", qualifiedByName = "doubleToBigDecimal")
    @Mapping(source = "transactionId", target = "id")
    MoneyTransferRequest toMoneyTransferRequest(UUID transactionId, PlayerRequestMoneyDto moneyRequestDto);

    @Mapping(source = "moneyRequestDto.recipient", target = "moneyTo")
    @Mapping(source = "moneyRequestDto.amount", target = "amount", qualifiedByName = "doubleToBigDecimal")
    @Mapping(source = "transactionId", target = "id")
    MoneyTransferRequest toMoneyTransferRequest(UUID transactionId, PlayerTransferMoneyRequestDto moneyRequestDto);

    @Mapping(source = "requester.id", target = "id")
    @Mapping(source = "requester.username", target = "username")
    @Mapping(source = "requester.balance", target = "balance")
    BalanceDto toBalanceDto(MoneyTransferResponse moneyTransferResponse);
    @Named("doubleToBigDecimal")
    static BigDecimal doubleToBigDecimal(Double val) {
        return BigDecimal.valueOf(val);
    }
}
