package com.savt.listopia.payload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieFrontDTO {
    private Integer movieId;

    private String title;

    private String poster;

    private Integer watchCount;

    private Integer likeCount;

    private Double ratingAverage;

    private Long clickCount;
}
