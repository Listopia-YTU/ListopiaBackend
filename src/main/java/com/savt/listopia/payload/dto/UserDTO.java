package com.savt.listopia.payload.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private String uuid;

    private String username;
    private String firstName;
    private String lastName;
}
