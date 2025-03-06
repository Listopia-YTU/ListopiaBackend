package com.savt.listopia.middleware;

import com.savt.listopia.model.user.Session;
import com.savt.listopia.model.user.User;
import com.savt.listopia.security.auth.*;
import com.savt.listopia.service.SessionService;
import com.savt.listopia.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthMiddleware extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthMiddleware.class);
    @Autowired
    SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // Gelen Request içinden auth token'i al ve parse et
        String token = getTokenFromRequest(request);

        if (token != null) {
            // Eğer session yoksa kullanıcı rastgele string girmiş olabilir SessionID kısmına
            // veya session'un süresi dolmuştur.
            Session session = sessionService.getSessionByUuid(token);

            if (session != null) {
                AuthenticationToken auth = new AuthenticationToken(session);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    String getTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("_SESSION")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
