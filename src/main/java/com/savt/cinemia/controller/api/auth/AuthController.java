package com.savt.cinemia.controller.api.auth;

import com.savt.cinemia.security.request.SignInRequestBodyPB;
import com.savt.cinemia.security.request.SignupRequestBodyPB;
import com.savt.cinemia.security.response.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignupRequestBodyPB signUpRequest) {
        return ResponseEntity.ok(new MessageResponse(""));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInRequestBodyPB signInRequest) {
        return ResponseEntity.ok(new MessageResponse(""));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOut() {
        return ResponseEntity.ok(new MessageResponse("You've been signed out!"));
    }
}
