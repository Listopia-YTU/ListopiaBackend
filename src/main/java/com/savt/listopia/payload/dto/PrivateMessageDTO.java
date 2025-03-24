package com.savt.listopia.payload.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PrivateMessageDTO {
    Long id;

    String fromUserUUID;

    String toUserUUID;

    Long sentAtTimestampSeconds;

    String message;
}
