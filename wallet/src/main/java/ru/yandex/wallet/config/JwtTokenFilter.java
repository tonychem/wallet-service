package ru.yandex.wallet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import ru.yandex.wallet.exception.ApiException;
import ru.yandex.wallet.exception.model.InvalidTokenException;
import ru.yandex.wallet.util.JwtUtils;

import java.io.IOException;

/**
 * Фильтр, ответственный за валидацию JWT в запросе. Пути запросов, указанные в множестве jwtIndependentRequestMappings,
 * игнорируются. Остальные - фильтруются с проверкой наличия заголовка Authorization в запросе и валидности токена.
 */
@RequiredArgsConstructor
public class JwtTokenFilter implements Filter {

    private final ObjectMapper mapper;

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            validateJwtToken(request);
        } catch (Exception e) {
            ApiException apiException = new ApiException(e.getMessage());
            response.getOutputStream().write(mapper.writeValueAsBytes(apiException));
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Метод проверяет наличие JWT в заголовке и валидирует его
     */
    private void validateJwtToken(HttpServletRequest request) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            String token = authHeader.substring(7);
            JwtUtils.isTokenValid(token, secret);
        } else {
            throw new InvalidTokenException("Отсутствует JWT");
        }
    }
}
