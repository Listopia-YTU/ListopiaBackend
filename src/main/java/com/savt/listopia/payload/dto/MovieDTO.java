package com.savt.listopia.payload.dto;

import com.savt.listopia.model.core.Genre;
import com.savt.listopia.model.movie.MovieCast;
import com.savt.listopia.model.movie.MovieCrew;
import com.savt.listopia.model.movie.MovieKeyword;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTO {
    private Integer movieId;

    private String originalLanguage;

    private String originalTitle;

    private String title;

    private String overview;

    private String tagline;

    private String releaseDate;

    private String trailerLink;

    private Integer runtime;

    private String backdrop;

    private String poster;

    private String logo;

    @ToString.Exclude
    private List<Genre> genres;

    // Rating stats //

    private Integer watchCount;

    private Integer likeCount;

    private Double ratingAverage;

    private Integer ratingCount;

    private Long clickCount;

    // External Ids //

    private String imdbId;

    private String wikidataId;

    private String facebookId;

    private String instagramId;

    private String twitterId;

}
