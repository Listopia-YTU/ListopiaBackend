package com.savt.listopia.model.people;

import com.savt.listopia.model.core.image.PersonImage;
import com.savt.listopia.model.movie.MovieCast;
import com.savt.listopia.model.movie.MovieCrew;
import com.savt.listopia.model.translation.PersonTranslation;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "persons")
public class Person {
    @Id
    private String imdbId;

    @Column(columnDefinition = "TEXT", length = 512)
    private String biography;

    private String name;

    private String birthday;

    @Nullable
    private String deathDay;

    private String placeOfBirth;

    private Double popularity;

    private String gender;

    private String knownForDepartment;

    @Nullable
    private String profilePath;

    @OneToMany(mappedBy = "person", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<PersonTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<PersonImage> profiles = new ArrayList<>();
}
