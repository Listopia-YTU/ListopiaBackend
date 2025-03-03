package com.savt.listopia.model.translation;

import com.savt.listopia.model.core.Genre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "genre_translations")
public class GenreTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer translationId;

    @ManyToOne
    private Genre genre;

    private String language;

    private String name;
}
