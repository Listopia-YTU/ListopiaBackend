package com.savt.listopia.payload.response;

import com.savt.listopia.model.movie.MovieCrew;
import com.savt.listopia.payload.dto.MovieCastDTO;
import com.savt.listopia.payload.dto.MovieCrewDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieCrewResponse {
    List<MovieCrewDTO> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
}
