package com.savt.cinemia.payload.dto;

import com.savt.cinemia.config.validator.EnumValidator;
import com.savt.cinemia.model.movie.ReleaseStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Long movieId;

    @NotBlank
    private String title;

    private String summary;

    /**
     * Minute
     */
    @Min(0)
    private Integer length;

    private String smallPoster;

    private String largePoster;

    private String trailer;

    private Float imdbScore;

    private Float rtScore;

    @EnumValidator(enumClass = ReleaseStatus.class)
    private String releaseStatus;

    // Implement Genre
}
