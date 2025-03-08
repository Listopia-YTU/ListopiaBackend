package com.savt.listopia.payload.dto;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieTranslationDTO {
    private Long translationId;
    @NotNull
    private String language;
    private String title;
    private String overview;
    private String tagline;
}
