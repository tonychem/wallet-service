package ru.tonychem.in.mapper;

import ru.tonychem.domain.dto.PlayerCreationRequest;
import ru.tonychem.in.dto.UnsecuredPlayerRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlayerRequestMapper extends Encodable {
    PlayerRequestMapper INSTANCE = Mappers.getMapper(PlayerRequestMapper.class);

    @Mapping(source = "login", target = "login")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    PlayerCreationRequest toPlayerCreationRequest(UnsecuredPlayerRequestDto unsecuredPlayerRequestDto);
}
