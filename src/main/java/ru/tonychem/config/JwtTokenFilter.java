package ru.tonychem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.tonychem.exception.model.InvalidTokenException;
import ru.tonychem.exception.ApiException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.tonychem.util.JwtUtils;

import java.io.IOException;
import java.util.Set;

/**
 * Фильтр, ответственный за валидацию JWT в запросе. Пути запросов, указанные в множестве jwtIndependentRequestMappings,
 * игнорируются. Осталльные - фильтруются с проверкой наличия заголовка Authorization в запросе и валидности токена.
 */
public class JwtTokenFilter implements Filter {

    private final ObjectMapper mapper;

    private final Set<String> jwtIndependentRequestMappings;

    public JwtTokenFilter(ObjectMapper mapper) {
        this.mapper = mapper;
        jwtIndependentRequestMappings = Set.of("/login", "/registration");
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String requestMapping = extractMapping(req);

        if (jwtIndependentRequestMappings.contains(requestMapping)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            validateJwtToken(req);
        } catch (Exception e) {
            ApiException apiException = new ApiException(e.getMessage());
            resp.getOutputStream().write(mapper.writeValueAsBytes(apiException));
            return;
        }

        chain.doFilter(req, resp);
    }

    /**
     * Извлекает путь запроса к контроллеру.
     */
    private String extractMapping(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    /**
     * Метод проверяет наличие JWT в заголовке и валидирует его
     */
    private void validateJwtToken(HttpServletRequest request) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            String token = authHeader.substring(7);
            JwtUtils.isTokenValid(token);
        } else {
            throw new InvalidTokenException("Отсутствует JWT");
        }
    }
}
