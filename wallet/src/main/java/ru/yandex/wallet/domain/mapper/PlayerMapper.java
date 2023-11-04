package ru.yandex.wallet.domain.mapper;

import ru.yandex.wallet.domain.Player;
import ru.yandex.wallet.domain.dto.AuthenticatedPlayerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.yandex.wallet.domain.dto.BalanceDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlayerMapper {
    PlayerMapper INSTANCE = Mappers.getMapper(PlayerMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "balance", target = "balance")
    AuthenticatedPlayerDto toAuthenticatedPlayerDto(Player player);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "balance", target = "balance")
    BalanceDto toBalanceDto(Player player);
}
