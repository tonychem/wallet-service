package ru.tonychem.config;

import liquibase.integration.spring.SpringLiquibase;
import org.aspectj.lang.Aspects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.tonychem.aop.ControllerAuditAspect;
import ru.tonychem.logging.Logger;
import ru.tonychem.logging.PGSQLLoggerImpl;
import ru.tonychem.util.JwtUtils;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.sql.DataSource;

@Configuration
@EnableAspectJAutoProxy
@EnableSwagger2
public class ApplicationConfiguration {
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

    /**
     * Конфигурация объекта для считывания свйоств из внешнего yaml - файла
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
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
    public Logger logger() {
        return new PGSQLLoggerImpl(jdbcUsername, jdbcPassword, jdbcURL, domainSchemaName);
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

    @Bean
    public Docket openApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("ru.tonychem.in.controller"))
                .paths(PathSelectors.any())
                .build();

        docket.useDefaultResponseMessages(false);
        return docket;
    }
}
