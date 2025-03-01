package com.savt.listopia.security.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequestBodyPB {
    @NotBlank private String email;

    @NotBlank private String password;
}
