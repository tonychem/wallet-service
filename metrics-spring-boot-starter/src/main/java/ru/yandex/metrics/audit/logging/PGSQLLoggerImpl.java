package ru.yandex.metrics.audit.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PGSQLLoggerImpl implements Logger {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void logMessage(LoggingLevel level, String message) {
        String insertLogQuery = """
                INSERT INTO logs(datetime, level, message) VALUES (?,?,?)
                """;
        jdbcTemplate.update(insertLogQuery, LocalDateTime.now(), level.name(), message);
    }
}
