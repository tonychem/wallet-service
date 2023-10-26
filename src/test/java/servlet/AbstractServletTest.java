package servlet;

import application.ApplicationController;
import config.ValidatingObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.LifecycleException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import util.ConfigFileReader;
import utils.HttpClient;
import utils.TomcatRunner;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractServletTest {
    protected static ApplicationController mockApplicationController;
    protected final ValidatingObjectMapper mapper = new ValidatingObjectMapper();
    protected static ExecutorService threadPool;
    protected static TomcatRunner tomcatRunner;

    protected static HttpClient httpClient;

    protected static Properties properties;

    /**
     * Метод читает тестовую конфигурацию, инициализирует пул потоков и запускает инстанс Tomcat в параллельном потоке
     */
    @BeforeAll
    public static void initiateBaseClass() throws IOException {
        properties = ConfigFileReader.read("application-test.properties");
        httpClient = new HttpClient();
        threadPool = Executors.newFixedThreadPool(2);
        tomcatRunner = new TomcatRunner();
        threadPool.execute(tomcatRunner);
    }

    @BeforeEach
    public void init() {
        mockApplicationController = Mockito.mock(ApplicationController.class);
    }

    @AfterAll
    public static void destroy() throws LifecycleException {
        tomcatRunner.destroy();
        threadPool.shutdownNow();
    }

    /**
     * Вычитывает путь к ресурсу, который соответствует указанному сервлету
     */
    protected static <T extends HttpServlet> String extractServletResourceUrl(Class<T> clazz) {
        return clazz.getAnnotation(WebServlet.class)
                .value()[0];
    }
}
