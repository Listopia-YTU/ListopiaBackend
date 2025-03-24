package com.savt.listopia.payload.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MovieCommentDTO {
    Long id;
    String userUUID;
    Integer movieId;
    Long sentAtTimestampSeconds;
    String message;
}
