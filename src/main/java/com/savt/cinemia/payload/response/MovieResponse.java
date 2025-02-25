package com.savt.cinemia.payload.response;

import com.savt.cinemia.payload.dto.MovieDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    private List<MovieDTO> content;
}
