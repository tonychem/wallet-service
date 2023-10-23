package logging;

import java.util.Properties;

public class LoggerFactory {
    private static Logger logger;

    private LoggerFactory() {
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = new PGSQLLoggerImpl();
        }
        return logger;
    }

    public static Logger getLogger(Properties properties) {
        if (logger == null) {
            String dbUsername = properties.getProperty("jdbc.username");
            String dbPassword = properties.getProperty("jdbc.password");
            String dbSchema = properties.getProperty("domain.schema.name");
            String dbURL = properties.getProperty("jdbc.url");
            logger = new PGSQLLoggerImpl(dbUsername, dbPassword, dbURL, dbSchema);
        }
        return logger;
    }
}
