package ru.tonychem.config;

import liquibase.integration.spring.SpringLiquibase;
import org.aspectj.lang.Aspects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.tonychem.aop.ControllerAuditAspect;
import ru.tonychem.aop.PerformanceAspect;
import ru.tonychem.application.ApplicationController;
import ru.tonychem.application.model.mapper.AuthenticationMapper;
import ru.tonychem.domain.mapper.PlayerMapper;
import ru.tonychem.domain.mapper.TransactionMapper;
import ru.tonychem.logging.Logger;
import ru.tonychem.logging.PGSQLLoggerImpl;
import ru.tonychem.repository.PlayerCrudRepository;
import ru.tonychem.repository.TransactionCrudRepository;
import ru.tonychem.repository.jdbcimpl.PGJDBCPlayerCrudRepositoryImpl;
import ru.tonychem.repository.jdbcimpl.PGJDBCTransactionCrudRepositoryImpl;
import ru.tonychem.service.PlayerService;
import ru.tonychem.service.PlayerServiceImpl;
import ru.tonychem.util.JwtUtils;

import javax.sql.DataSource;

@Configuration
public class BeanConfiguration {

    @Value("${spring.datasource.url}")
    private String jdbcURL;

    @Value("${spring.datasource.username}")
    private String jdbcUsername;

    @Value("${spring.datasource.password}")
    private String jdbcPassword;

    @Value("${liquibase.schema}")
    private String migrationSchemaName;

    @Value("${schema.domain.name}")
    private String domainSchemaName;

    @Value("${liquibase.changeLogFile}")
    private String liquibaseChangeLogFilePath;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

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

    @Bean
    public DataSource pgsqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(jdbcURL);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        return dataSource;
    }

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setLiquibaseSchema(migrationSchemaName);
        liquibase.setDefaultSchema(domainSchemaName);
        liquibase.setChangeLog(liquibaseChangeLogFilePath);
        liquibase.setDataSource(pgsqlDataSource());
        return liquibase;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(pgsqlDataSource());
    }

    @Bean
    public Logger logger() {
        return new PGSQLLoggerImpl(jdbcUsername, jdbcPassword, jdbcURL, domainSchemaName);
    }

    @Bean
    public PerformanceAspect performanceAspect() {
        PerformanceAspect aspect = Aspects.aspectOf(PerformanceAspect.class);
        return aspect;
    }

    @Bean
    public ControllerAuditAspect controllerAuditAspect() {
        ControllerAuditAspect aspect = Aspects.aspectOf(ControllerAuditAspect.class);
        aspect.setLogger(logger());
        return aspect;
    }

    @Bean
    public void setJwtUtilsSecret() {
        JwtUtils.setSecret(jwtSecret);
    }
}
