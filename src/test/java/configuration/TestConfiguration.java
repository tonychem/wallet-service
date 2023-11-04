package configuration;

import org.aspectj.lang.Aspects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.tonychem.aop.ControllerAuditAspect;
import ru.tonychem.logging.Logger;
import ru.tonychem.logging.PGSQLLoggerImpl;
import ru.tonychem.util.JwtUtils;

@Configuration
@EnableAspectJAutoProxy
public class TestConfiguration {
    @Value("${schema.domain.name}")
    private String schema;

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Конфигурация объекта для чтения из внешнего yaml-файла
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-test.yml"));
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

    /**
     * Создает новый контейнер для логирования через аспекты
     */
    @Bean
    public Logger logger() {
        PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.0");
        postgreSQLContainer.withInitScript("create-log-table.sql");
        postgreSQLContainer.start();

        return new PGSQLLoggerImpl(postgreSQLContainer.getUsername(), postgreSQLContainer.getPassword(),
                postgreSQLContainer.getJdbcUrl(), schema);
    }

    /**
     * Аспект для логгирования контроллера
     */
    @Bean
    public ControllerAuditAspect controllerAuditAspect() {
        ControllerAuditAspect aspect = Aspects.aspectOf(ControllerAuditAspect.class);
        aspect.setLogger(logger());
        return aspect;
    }

    @Bean
    public void configureJwtUtils() {
        JwtUtils.setSecret(secret);
    }
}
