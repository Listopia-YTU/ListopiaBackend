package com.savt.listopia.model.core;

import com.savt.listopia.model.translation.GenreTranslation;
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
@Table(name = "genres")
public class Genre {
    @Id
    private Integer genreId;

    private String name;

    @OneToMany(mappedBy = "genre", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = true)
    private List<GenreTranslation> translations = new ArrayList<>();
}
