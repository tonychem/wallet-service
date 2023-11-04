package ru.tonychem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

public class AppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext container) {

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.scan("ru.tonychem");

        container.addListener(new ContextLoaderListener(context));
        container.addFilter("jwt-filter", new JwtTokenFilter(new ObjectMapper()))
                .addMappingForUrlPatterns(null, true, "/logout", "/player-management/*");

        ServletRegistration.Dynamic dispatcher = container.addServlet("mvc", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
    }
}