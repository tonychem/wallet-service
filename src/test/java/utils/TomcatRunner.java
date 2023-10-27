package utils;

import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.nio.file.Paths;

/**
 * Класс, ответственный за програмнный запуск сервера Tomcat.
 */
public class TomcatRunner implements Runnable {
    private static final String HOST = "localhost";
    private static final int TEST_PORT = 8182;

    private static final String testApplicationRootPattern = "/test/*";

    private final Tomcat tomcat;

    private Context context;

    public TomcatRunner() {
        tomcat = new Tomcat();
        context = tomcat.addContext("", Paths.get(".").toString());
    }

    /**
     * Запуск Tomcat сервера
     */
    @Override
    public void run() {
        tomcat.setPort(TEST_PORT);
        tomcat.setHostname("localhost");
        String appBase = ".";
        tomcat.getHost().setAppBase(appBase);

        tomcat.getConnector();

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Динамическая регистрация сервлета на сервере
     */
    public void registerServlet(HttpServlet servlet) {
        Class<?> servletClass = servlet.getClass();
        Tomcat.addServlet(context, servletClass.getSimpleName(), servlet);
        context.addServletMappingDecoded(testApplicationRootPattern, servletClass.getSimpleName());
    }

    /**
     * Динамически удалить сервлет
     */
    public void removeServlet(HttpServlet servlet) {
        Container child = context.findChild(servlet.getClass().getSimpleName());
        context.removeChild(child);
    }

    /**
     * Остановить работу Tomcat
     */
    public void destroy() throws LifecycleException {
        tomcat.stop();
        tomcat.destroy();
    }

    /**
     * Возвращает корневой url томкат сервера
     */
    public String getApplicationUrl() {
        return "http://" + HOST + ":" + TEST_PORT
                + testApplicationRootPattern.substring(0, testApplicationRootPattern.length() - 2);
    }
}
