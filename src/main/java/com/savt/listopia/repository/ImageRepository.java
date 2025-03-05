package com.savt.listopia.repository;

import com.savt.listopia.model.core.image.MovieImage;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<MovieImage, Long> {
    @Query("SELECT i.filePath FROM MovieImage i JOIN i.movie m WHERE m.movieId = :movieId")
    String findMovieImageByMovieId(Integer movieId, Limit limit);

    @Query("SELECT i.filePath FROM MovieImage i WHERE i.type = :type AND i.movie.movieId = :movieId")
    String findMovieImageByMovieIdAndType(Integer movieId, Limit of, int type);
}
