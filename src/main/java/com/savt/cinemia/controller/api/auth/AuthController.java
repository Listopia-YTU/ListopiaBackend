package com.savt.cinemia.controller.api.auth;

import com.savt.cinemia.middleware.AuthMiddleware;
import com.savt.cinemia.security.auth.JWTIssuer;
import com.savt.cinemia.security.auth.SessionManager;
import com.savt.cinemia.security.auth.UserPrinciple;
import com.savt.cinemia.security.request.SignInRequestBodyPB;
import com.savt.cinemia.security.request.SignUpRequestBodyPB;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final JWTIssuer jwtService;
    private final SessionManager sessionManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthMiddleware.class);

    public AuthController(JWTIssuer jwtService, SessionManager sessionManager) {
        this.jwtService = jwtService;
        this.sessionManager = sessionManager;
    }
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(
        @Valid @RequestBody SignUpRequestBodyPB signUpRequest, HttpSession session) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(
        @RequestBody SignInRequestBodyPB signInRequest, HttpServletResponse response) {
        Cookie token = sessionManager.createCookieForUserId("test1");
        response.addCookie(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOut(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        LOGGER.warn("auth:" + auth);
        return ResponseEntity.ok().build();
    }
}
