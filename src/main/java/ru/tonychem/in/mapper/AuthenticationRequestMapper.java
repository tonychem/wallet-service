package ru.tonychem.in.mapper;

import ru.tonychem.application.model.dto.AuthenticationRequest;
import ru.tonychem.in.dto.UnsecuredAuthenticationRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthenticationRequestMapper extends Encodable {
    AuthenticationRequestMapper INSTANCE = Mappers.getMapper(AuthenticationRequestMapper.class);

    @Mapping(source = "login", target = "login")
    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    AuthenticationRequest toAuthenticationRequest(UnsecuredAuthenticationRequestDto unsecuredAuthenticationRequestDto);
}
