package com.savt.listopia.model.translation;

import com.savt.listopia.model.people.Person;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "person_translations")
public class PersonTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer translationId;

    private String language;

    @Column(columnDefinition="TEXT", length = 512)
    private String biography;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;
}
