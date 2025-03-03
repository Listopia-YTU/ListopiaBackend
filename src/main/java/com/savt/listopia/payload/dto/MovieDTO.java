package com.savt.listopia.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTO {
    private Long movieId;

    private String originalLanguage;

    private String originalTitle;

    private String title;

    private String overview;

    private String tagline;

    private Double popularity;

    private String releaseDate;

    private String trailerLink;

    private Integer runtime;
}
