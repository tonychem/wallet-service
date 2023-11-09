package ru.yandex.wallet.domain.mapper;

import ru.yandex.wallet.domain.Transaction;
import ru.yandex.wallet.domain.dto.MoneyTransferRequest;
import ru.yandex.wallet.domain.dto.TransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    TransactionDto toTransactionDto(Transaction transaction);

    @Mapping(source = "sender", target = "moneyFrom")
    @Mapping(source = "recipient", target = "moneyTo")
    MoneyTransferRequest toMoneyTransferRequest(Transaction transaction);
}
