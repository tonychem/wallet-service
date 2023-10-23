package in.servlet;

import application.model.dto.AuthenticationDto;
import config.ValidatingObjectMapper;
import exception.InvalidTokenException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import util.JwtUtils;

import java.util.*;

/**
 * Абстрактный HttpServlet, содержащий общую логику для всех сервлетов приложения.
 */
public abstract class AbstractServiceServlet extends HttpServlet {
    protected final ValidatingObjectMapper mapper;

    public AbstractServiceServlet() {
        mapper = new ValidatingObjectMapper();
    }

    protected String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer")) {
            return authorization.substring(7);
        } else {
            throw new InvalidTokenException("Токен отсутствует или некорректен");
        }
    }

    protected String produceJwt(AuthenticationDto authentication) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", authentication.getId());
        claims.put("login", authentication.getLogin());
        claims.put("username", authentication.getUsername());
        claims.put("session-id", authentication.getSessionId());

        return JwtUtils.generateToken(claims);
    }

    protected List<UUID> extractValidUUIDsFromString(Collection<String> ids) {
        List<UUID> result = new ArrayList<>(ids.size());

        for (String id : ids) {
            try {
                result.add(UUID.fromString(id));
            } catch (IllegalArgumentException e) {
            }
        }

        return result;
    }
}
