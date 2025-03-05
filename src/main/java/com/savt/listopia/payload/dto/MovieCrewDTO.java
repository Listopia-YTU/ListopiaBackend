package com.savt.listopia.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieCrewDTO {
    private String originalName;

    private String profilePath;

    private String department;

    private String job;

    private String personId;
}
