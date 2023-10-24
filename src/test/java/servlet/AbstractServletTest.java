package servlet;

import application.ApplicationController;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ValidatingObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import util.ConfigFileReader;

import java.io.IOException;
import java.util.Properties;

public abstract class AbstractServletTest {
    protected ApplicationController applicationController;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected final ObjectMapper mapper = new ValidatingObjectMapper();

    protected static Properties properties;

    @BeforeAll
    public static void readProperties() throws IOException {
        properties = ConfigFileReader.read("application-test.properties");
    }

    @BeforeEach
    public void init() {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.spy(HttpServletResponse.class);
        applicationController = Mockito.mock(ApplicationController.class);
    }
}
