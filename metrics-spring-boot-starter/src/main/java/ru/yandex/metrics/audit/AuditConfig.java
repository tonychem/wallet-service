package ru.yandex.metrics.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.metrics.audit.logging.PGSQLLoggerImpl;

@Configuration
@EnableAspectJAutoProxy
@ConditionalOnBean(JdbcTemplate.class)
public class AuditConfig {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public AuditConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public ControllerAuditAspect controllerAuditAspect() {
        return new ControllerAuditAspect(new PGSQLLoggerImpl(jdbcTemplate));
    }
}
