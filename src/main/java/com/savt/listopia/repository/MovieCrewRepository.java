package com.savt.listopia.repository;

import com.savt.listopia.model.movie.MovieCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieCrewRepository extends JpaRepository<MovieCrew, String> {
    MovieCrew getMovieCrewByCrewId(String creditId);
}
