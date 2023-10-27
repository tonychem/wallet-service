import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.postgresql.Driver;
import util.ConfigFileReader;
import util.MigrationTool;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@WebListener
public class WalletApplication implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Properties properties = ConfigFileReader.read("application.properties");
            DriverManager.registerDriver(new Driver());
            MigrationTool.applyMigration(properties);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
