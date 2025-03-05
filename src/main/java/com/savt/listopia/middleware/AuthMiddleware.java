package com.savt.listopia.middleware;

import com.savt.listopia.security.auth.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthMiddleware extends OncePerRequestFilter {
    private final SessionManager sessionManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthMiddleware.class);

    public AuthMiddleware(SessionManager sessionManager) { this.sessionManager = sessionManager; }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

        // Gelen Request içinden auth token'i al ve parse et
        String token = getTokenFromRequest(request);

        if (token != null) {
            // Eğer session yoksa kullanıcı rastgele string girmiş olabilir SessionID kısmına
            // veya session'un süresi dolmuştur.
            Session session = sessionManager.getSession(token);

            if (session != null) {
                LOGGER.warn("SessionId: " + session.getSessionId());

                // Eğer sessionmanager, principle üretemezse sebebi Session'un id'si herhangi bir
                // user'de bulunmamış olması olabilir.
                UserPrinciple principle = sessionManager.getPrinciple(session);

                if (principle != null) {
                    UserPrincipleAuthToken auth = new UserPrincipleAuthToken(principle);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    LOGGER.warn(
                        "UserPrinciple not found for sessionIdd: {}!", session.getSessionId());
                }
            }
        }

        chain.doFilter(request, response);
    }

    String getTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("_SESSIONID")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
