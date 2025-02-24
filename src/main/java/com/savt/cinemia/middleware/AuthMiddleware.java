package com.savt.cinemia.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import com.savt.cinemia.model.User;

@Component
public class AuthMiddleware extends OncePerRequestFilter {
    public static final String USER_ATTR = "User";
    public static final String USER_UUID_SESS_ATTR = "user_uuid";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthMiddleware.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        User user = null;

        Object user_uuid = request.getSession().getAttribute(USER_UUID_SESS_ATTR);
        if (user_uuid != null) {
            if ( user_uuid instanceof String uuid ) {
                user = new User(uuid);
            }
        }

        LOGGER.debug("user: {}", user);

        request.setAttribute(USER_ATTR, user);
        chain.doFilter(request, response);
    }
}
