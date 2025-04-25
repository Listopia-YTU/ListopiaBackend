package com.savt.listopia.controller;

import com.savt.listopia.model.user.Session;
import com.savt.listopia.model.user.User;
import com.savt.listopia.payload.APIResponse;
import com.savt.listopia.security.request.SignInRequestBody;
import com.savt.listopia.security.request.SignUpRequestBody;
import com.savt.listopia.service.SessionService;
import com.savt.listopia.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    UserService userService;
    @Autowired
    SessionService sessionService;

    @PostMapping("/signup")
    public ResponseEntity<APIResponse> signUp(@Valid @RequestBody SignUpRequestBody signUpRequest, HttpServletResponse response) {
        User user = userService.registerUser(
            signUpRequest.getFirstName(),
            signUpRequest.getLastName(),
            signUpRequest.getEmail(),
            signUpRequest.getUsername(),
            signUpRequest.getPassword()
        );

        Session userSession = sessionService.createSession(user);
        ResponseCookie sessionCookie = sessionService.createCookie(userSession);
        response.setHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());
        return ResponseEntity.ok(APIResponse.builder().success(true).message("user_created").build());
    }

    @PostMapping("/signin")
    public ResponseEntity<APIResponse> signIn(@Valid @RequestBody SignInRequestBody signInRequest, HttpServletResponse response) {
        User user = userService.getUserByEmailPassword(
            signInRequest.getEmail(),
            signInRequest.getPassword());

        Session userSession = sessionService.createSession(user);
        ResponseCookie sessionCookie = sessionService.createCookie(userSession);
        response.setHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());
        return ResponseEntity.ok(APIResponse.builder().success(true).message("logged_in").build());
    }

    @PostMapping("/signout")
    public ResponseEntity<APIResponse> signOut(HttpServletResponse response) {
        Session session1 = sessionService.getCurrentSession();
        sessionService.deleteSession(session1);

        Cookie deleteCookie = new Cookie("_SESSION", "");
        deleteCookie.setMaxAge(0);
        deleteCookie.setPath("/");
        response.addCookie(deleteCookie);

        SecurityContextHolder.getContext().setAuthentication(null);

        return ResponseEntity.ok(APIResponse.builder().success(true).message("logged_out").build());
    }

}
