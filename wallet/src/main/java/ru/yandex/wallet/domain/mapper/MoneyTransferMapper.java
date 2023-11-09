package ru.yandex.wallet.domain.mapper;

import model.dto.out.BalanceDto;
import model.dto.in.PlayerRequestMoneyDto;
import model.dto.in.PlayerTransferMoneyRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.yandex.wallet.domain.dto.MoneyTransferRequest;
import ru.yandex.wallet.domain.dto.MoneyTransferResponse;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MoneyTransferMapper {
    MoneyTransferMapper INSTANCE = Mappers.getMapper(MoneyTransferMapper.class);

    @Mapping(source = "moneyRequestDto.donor", target = "moneyFrom")
    @Mapping(source = "moneyRequestDto.amount", target = "amount", qualifiedByName = "doubleToBigDecimal")
    @Mapping(source = "transactionId", target = "id")
    @Mapping(source = "recipient", target = "moneyTo")
    MoneyTransferRequest toMoneyTransferRequest(UUID transactionId, String recipient,
                                                PlayerRequestMoneyDto moneyRequestDto);

    @Mapping(source = "moneyRequestDto.recipient", target = "moneyTo")
    @Mapping(source = "moneyRequestDto.amount", target = "amount", qualifiedByName = "doubleToBigDecimal")
    @Mapping(source = "transactionId", target = "id")
    @Mapping(source = "donor", target = "moneyFrom")
    MoneyTransferRequest toMoneyTransferRequest(UUID transactionId, String donor,
                                                PlayerTransferMoneyRequestDto moneyRequestDto);

    @Mapping(source = "requester.id", target = "id")
    @Mapping(source = "requester.username", target = "username")
    @Mapping(source = "requester.balance", target = "balance")
    BalanceDto toBalanceDto(MoneyTransferResponse moneyTransferResponse);
    @Named("doubleToBigDecimal")
    static BigDecimal doubleToBigDecimal(Double val) {
        return BigDecimal.valueOf(val);
    }
}
