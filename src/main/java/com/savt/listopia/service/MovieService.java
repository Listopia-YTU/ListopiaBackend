package com.savt.listopia.service;

import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.response.MovieFrontResponse;

public interface MovieService {
    MovieFrontResponse getFrontMovies(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String genre, String language);

    MovieDTO getMovie(Integer movieId, String language);
}
