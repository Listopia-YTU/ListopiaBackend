package com.savt.listopia.repository;

import com.savt.listopia.model.user.MovieComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieCommentRepository extends JpaRepository<MovieComment, Long> {
    Page<MovieComment> findByMovie_MovieId(Integer movieId, Pageable pageable);
    Page<MovieComment> findByFromUser_Id(Long userId, Pageable pageable);
    Page<MovieComment> findByFromUser_IdAndMovie_MovieId(Long userId, Integer movieId, Pageable pageable);
    Page<MovieComment> findByIsReported(Boolean isReported, Pageable pageable);
    Page<MovieComment> findByMessageContainingIgnoreCase(String text, Pageable pageable);
    Page<MovieComment> findByMovie_MovieIdOrderBySentAtTimestampSecondsDesc(Integer movieId, Pageable pageable);
}
