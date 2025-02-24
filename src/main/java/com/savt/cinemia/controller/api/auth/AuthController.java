package com.savt.cinemia.controller.api.auth;

import com.savt.cinemia.middleware.AuthMiddleware;
import com.savt.cinemia.security.auth.UserPrinciple;
import com.savt.cinemia.security.request.SignInRequestBodyPB;
import com.savt.cinemia.security.request.SignUpRequestBodyPB;
import com.savt.cinemia.security.response.MessageResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthMiddleware.class);

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequestBodyPB signUpRequest , HttpSession session) {
        session.setAttribute(AuthMiddleware.USER_UUID_SESS_ATTR, "test_uuid");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInRequestBodyPB signInRequest) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOut(HttpSession session) {
        UserPrinciple principle = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LOGGER.warn("username:"+principle.getName());
        session.setAttribute(AuthMiddleware.USER_UUID_SESS_ATTR, null);
        return ResponseEntity.ok().build();
    }
}
