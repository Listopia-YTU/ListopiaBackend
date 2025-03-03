package com.savt.listopia.repository;

import com.savt.listopia.model.movie.MovieCast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieCastRepository extends JpaRepository<MovieCast, String> {
    MovieCast getMovieCastByCastId(String creditId);
}
