package com.savt.listopia.payload.dto;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.people.Person;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieCastDTO {
    private String originalName;

    private String profilePath;

    private String character;

    private String personId;
}
