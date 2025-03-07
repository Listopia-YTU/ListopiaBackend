package com.savt.listopia.model.translation;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JoinColumn(name = "translation_id")
    private Integer translationId;

    private String language;

    private String name;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "genre_id")
    private Genre genre;
}
