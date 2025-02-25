package com.savt.cinemia.model.movie;

import com.savt.cinemia.config.validator.EnumValidator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenre {
    @Id
    @EnumValidator(enumClass = Genre.class)
    @Column(name = "genre_name")
    private String genreName;
}
