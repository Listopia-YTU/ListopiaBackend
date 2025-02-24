package com.savt.cinemia.middleware;

import com.savt.cinemia.model.User;
import com.savt.cinemia.security.auth.UserAuthentication;
import com.savt.cinemia.security.auth.UserPrinciple;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AuthMiddleware extends OncePerRequestFilter {
    public static final String USER_ATTR = "User";
    public static final String USER_UUID_SESS_ATTR = "user_uuid";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthMiddleware.class);

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        User user = null;


        SecurityContextHolder.getContext().setAuthentication( new UserAuthentication( new UserPrinciple( "Ensar" )));

        // retrieve auth token from request
        Object user_uuid = request.getSession().getAttribute(USER_UUID_SESS_ATTR);
        if (user_uuid != null) {
            if (user_uuid instanceof String uuid) {
                user = new User(uuid);
            }
        }

        LOGGER.error("user: {}", user);

        if ( user != null ) {
            SecurityContextHolder.getContext().setAuthentication( new UserAuthentication( new UserPrinciple( "Ensar" )));
        }

        request.setAttribute(USER_ATTR, user);
        chain.doFilter(request, response);
    }
}
