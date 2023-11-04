package ru.tonychem.domain.mapper;

import ru.tonychem.domain.Transaction;
import ru.tonychem.domain.dto.MoneyTransferRequest;
import ru.tonychem.domain.dto.TransactionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    TransactionDto toTransactionDto(Transaction transaction);

    @Mapping(source = "sender", target = "moneyFrom")
    @Mapping(source = "recipient", target = "moneyTo")
    MoneyTransferRequest toMoneyTransferRequest(Transaction transaction);
}
