package com.savt.listopia.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieFrontDTO {
    private Long movieId;

    private String title;

    private String poster;
}
