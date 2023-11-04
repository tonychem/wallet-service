package ru.tonychem.domain.mapper;

import ru.tonychem.domain.Player;
import ru.tonychem.domain.dto.AuthenticatedPlayerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.tonychem.domain.dto.BalanceDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
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
