package ru.tonychem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tonychem.application.ApplicationController;
import ru.tonychem.application.model.mapper.AuthenticationMapper;
import ru.tonychem.domain.mapper.PlayerMapper;
import ru.tonychem.domain.mapper.TransactionMapper;
import ru.tonychem.repository.PlayerCrudRepository;
import ru.tonychem.repository.TransactionCrudRepository;
import ru.tonychem.repository.jdbcimpl.PGJDBCPlayerCrudRepositoryImpl;
import ru.tonychem.repository.jdbcimpl.PGJDBCTransactionCrudRepositoryImpl;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.PlayerServiceImpl;

@Configuration
public class BeanConfiguration {

    @Value("${spring.datasource.url}")
    private String jdbcURL;

    @Value("${spring.datasource.username}")
    private String jdbcUsername;

    @Value("${spring.datasource.password}")
    private String jdbcPassword;

    @Value("${schema.domain.name}")
    private String domainSchemaName;

    @Bean
    public PlayerCrudRepository playerCrudRepository() {
        return new PGJDBCPlayerCrudRepositoryImpl(jdbcURL, jdbcUsername, jdbcPassword, domainSchemaName);
    }

    @Bean
    public TransactionCrudRepository transactionCrudRepository() {
        return new PGJDBCTransactionCrudRepositoryImpl(jdbcURL, jdbcUsername, jdbcPassword, domainSchemaName);
    }

    @Bean
    public PlayerMapper playerMapper() {
        return PlayerMapper.INSTANCE;
    }

    @Bean
    public TransactionMapper transactionMapper() {
        return TransactionMapper.INSTANCE;
    }

    @Bean
    public PlayerService playerService() {
        return new PlayerServiceImpl(playerCrudRepository(), transactionCrudRepository(),
                playerMapper(), transactionMapper());
    }

    @Bean
    public AuthenticationMapper authenticationMapper() {
        return AuthenticationMapper.INSTANCE;
    }

    @Bean
    public ApplicationController applicationController() {
        return new ApplicationController(playerService(), authenticationMapper());
    }
}
