package ru.tonychem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.tonychem.exception.ApiException;
import ru.tonychem.exception.model.InvalidTokenException;
import ru.tonychem.util.JwtUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Фильтр, ответственный за валидацию JWT в запросе. Пути запросов, указанные в множестве jwtIndependentRequestMappings,
 * игнорируются. Остальные - фильтруются с проверкой наличия заголовка Authorization в запросе и валидности токена.
 */
public class JwtTokenFilter implements Filter {

    private final ObjectMapper mapper;

    public JwtTokenFilter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        try {
            validateJwtToken(req);
        } catch (Exception e) {
            ApiException apiException = new ApiException(e.getMessage());
            resp.getOutputStream().write(mapper.writeValueAsBytes(apiException));
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(req, resp);
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
