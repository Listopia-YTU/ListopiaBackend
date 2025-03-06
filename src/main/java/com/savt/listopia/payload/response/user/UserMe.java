package com.savt.listopia.payload.response.user;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserMe {
    String firstName;
    String lastName;
    String email;
    String uuid;
    String username;
}
