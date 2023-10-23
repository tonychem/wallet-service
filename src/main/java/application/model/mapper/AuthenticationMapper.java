package application.model.mapper;

import application.model.Authentication;
import application.model.dto.AuthenticationDto;
import application.model.dto.BalanceDto;
import domain.dto.AuthenticatedPlayerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthenticationMapper {
    AuthenticationMapper INSTANCE = Mappers.getMapper(AuthenticationMapper.class);

    @Mapping(source = "authentication.id", target = "id")
    @Mapping(source = "authentication.login", target = "login")
    @Mapping(source = "authentication.username", target = "username")
    @Mapping(source = "authentication.sessionId", target = "sessionId")
    @Mapping(source = "balance", target = "balance")
    AuthenticationDto toAuthenticationDto(Authentication authentication, BigDecimal balance);

    @Mapping(source = "playerDto.id", target = "id")
    @Mapping(source = "playerDto.login", target = "login")
    @Mapping(source = "playerDto.username", target = "username")
    @Mapping(source = "playerDto.balance", target = "balance")
    @Mapping(source = "sessionId", target = "sessionId")
    AuthenticationDto toAuthenticationDto(UUID sessionId, AuthenticatedPlayerDto playerDto);

    BalanceDto toBalanceDto(AuthenticatedPlayerDto playerDto);
}
