package com.savt.listopia.repository;

import com.savt.listopia.model.movie.MovieCast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieCastRepository extends JpaRepository<MovieCast, Integer> {
    MovieCast getMovieCastById(Integer creditId);

    Page<MovieCast> findAllByMovieMovieId(Integer movieId, Pageable pageDetails);

    List<MovieCast> findAllByMovieMovieId(Integer movieId);
}
