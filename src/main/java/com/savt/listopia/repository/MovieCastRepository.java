package com.savt.listopia.repository;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.movie.MovieCast;
import com.savt.listopia.payload.dto.MovieCastDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieCastRepository extends JpaRepository<MovieCast, Integer> {
    MovieCast getMovieCastByCastId(Integer creditId);

    Page<MovieCast> findAllByMovieMovieId(Integer movieId, Pageable pageDetails);
}
