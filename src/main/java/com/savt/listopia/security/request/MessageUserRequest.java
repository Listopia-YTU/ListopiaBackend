package com.savt.listopia.security.request;

import lombok.Data;

import java.util.UUID;

@Data
public class MessageUserRequest {
    UUID to;
    String message;
}
