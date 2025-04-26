package com.savt.listopia.service;

import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.payload.response.MovieResponse;

public interface StatService {
    MovieFrontResponse getMostLikedMovies(Integer pageNumber, Integer pageSize, String sortOrder, String language);
}
