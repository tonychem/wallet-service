package ru.tonychem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tonychem.repository.PlayerCrudRepository;
import ru.tonychem.repository.TransactionCrudRepository;
import ru.tonychem.repository.jdbcimpl.PGJDBCPlayerCrudRepositoryImpl;
import ru.tonychem.repository.jdbcimpl.PGJDBCTransactionCrudRepositoryImpl;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.impl.PlayerServiceImpl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    public MessageDigest messageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5");
    }

    @Bean
    public PlayerService playerService() throws NoSuchAlgorithmException {
        return new PlayerServiceImpl(playerCrudRepository(), transactionCrudRepository(), messageDigest());
    }
}
