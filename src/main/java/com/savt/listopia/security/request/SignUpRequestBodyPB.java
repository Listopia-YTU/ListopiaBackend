package com.savt.listopia.security.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Data;

@Data
public class SignUpRequestBodyPB {
    @NotBlank @Email private String email;

    @NotBlank private String password;

    @NotBlank private String firstName;

    @NotBlank private String lastName;

    private Set<String> role;
}
