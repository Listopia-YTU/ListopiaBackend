package com.savt.listopia.model.core.image;

import com.savt.listopia.model.people.Person;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "person_images")
public class PersonImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String filePath;

    private Double aspectRatio;

    private Integer height;

    private Integer width;

    private Integer type;

    @ManyToOne()
    @JoinColumn(name = "person_id")
    private Person person;
}
