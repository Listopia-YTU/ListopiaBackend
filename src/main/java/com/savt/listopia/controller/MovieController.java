package com.savt.listopia.controller;

import com.savt.listopia.config.AppConstants;
import com.savt.listopia.payload.APIResponse;
import com.savt.listopia.payload.dto.MovieCommentDTO;
import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.dto.MovieTranslationDTO;
import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.service.MovieService;
import com.savt.listopia.service.UserService;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class MovieController {
    private final MovieService movieService;
    private final UserService userService;

    public MovieController(MovieService movieService, UserService userService) {
        this.movieService = movieService;
        this.userService = userService;
    }

    @GetMapping("/movies/{movieId}")
    public ResponseEntity<MovieDTO> getMovie(@PathVariable Integer movieId,
                                             @RequestParam(name = "language", defaultValue = "en", required = false) String language) {
        MovieDTO movieDTO = movieService.getMovie(movieId, language);
        return new ResponseEntity<>(movieDTO, HttpStatus.OK);
    }

    @GetMapping("/movies/front")
    public ResponseEntity<MovieFrontResponse> getFrontMovies(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MOVIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "language", defaultValue = "en", required = false) String language) {
        MovieFrontResponse movieFrontResponse = movieService.getFrontMovies(pageNumber, pageSize, sortBy, sortOrder, genre, language);
        return new ResponseEntity<>(movieFrontResponse, HttpStatus.OK);
    }

    @GetMapping("/movies/front/search")
    public ResponseEntity<MovieFrontResponse> getFrontMoviesByWord(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MOVIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "language", defaultValue = "en", required = false) String language,
            @RequestParam String word) {
        MovieFrontResponse movieFrontResponse = movieService.getFrontMoviesByWord(pageNumber, pageSize, sortBy, sortOrder, genre, language, word);
        return new ResponseEntity<>(movieFrontResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/movies/{movieId}")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Integer movieId,
                                                @RequestBody MovieDTO movieDTO) {
        MovieDTO savedMovieDTO = movieService.updateMovie(movieId, movieDTO);
        return new ResponseEntity<>(savedMovieDTO, HttpStatus.OK);
    }

    @PostMapping("/admin/movies/{movieId}/translations")
    public ResponseEntity<MovieTranslationDTO> addTranslation(@PathVariable Integer movieId
            , @RequestBody MovieTranslationDTO movieTranslationDTO) {
        MovieTranslationDTO savedMovieTranslationDTO = movieService.addTranslation(movieId, movieTranslationDTO);
        return new ResponseEntity<>(savedMovieTranslationDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/movies/{movieId}/translations/{translationId}")
    public ResponseEntity<MovieTranslationDTO> deleteTranslation(@PathVariable Integer movieId, @PathVariable Long translationId) {
        MovieTranslationDTO deletedMovieTranslationDTO = movieService.deleteTranslation(movieId, translationId);
        return new ResponseEntity<>(deletedMovieTranslationDTO, HttpStatus.OK);
    }

    @PutMapping("/admin/movies/{movieId}/fetch")
    public ResponseEntity<MovieDTO> fetchFromExternalDb(@PathVariable Integer movieId) {
        MovieDTO fetchedMovieDTO = movieService.fetchFromExternalDb(movieId);
        return new ResponseEntity<>(fetchedMovieDTO, HttpStatus.OK);
    }

    @PostMapping("/movies/{movieId}/comment")
    public ResponseEntity<MovieCommentDTO> commentMovie(
            @PathVariable Integer movieId,
            @RequestParam(name = "message") String message,
            @RequestParam(name = "isSpoiler", required = false) Boolean isSpoiler
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        MovieCommentDTO dto = userService.createMovieComment(userId, movieId, isSpoiler, message);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/movies/{movieId}/comment")
    public ResponseEntity<Page<MovieCommentDTO>> getMovieComments(
            @PathVariable Integer movieId,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @Max(50) @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "fromUser", defaultValue = "", required = false) String fromUser
    ) {
        Page<MovieCommentDTO> dto;
        if ( fromUser.isEmpty() ) {
            dto = userService.getMovieCommentForMovie(movieId,pageNumber,pageSize);
        } else {
            Long userId = userService.getUserIdFromUUID(UUID.fromString(fromUser));
            dto = userService.getMovieCommentForMovieFromUser(movieId, userId, pageNumber, pageSize);
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/movies/comment/{commentId}/report")
    public ResponseEntity<APIResponse> reportMessage(
            @PathVariable Long commentId
    ) {
        userService.reportMovieComment(commentId);
        return ResponseEntity.ok(APIResponse.builder().success(true).message("movie_comment_reported").build());
    }

    @PutMapping("/movies/comment/{commentId}")
    public ResponseEntity<MovieCommentDTO> changeComment(
            @PathVariable Long commentId,
            @RequestParam(name = "message") String message,
            @RequestParam(name = "isSpoiler", required = false) Boolean isSpoiler
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        MovieCommentDTO dto = userService.updateMovieComment(userId, commentId, isSpoiler, message);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/movies/comment/{commentId}")
    public ResponseEntity<APIResponse> deleteComment(
            @PathVariable Long commentId
    ) {
        Long userId = userService.getCurrentUserIdOrThrow();
        userService.deleteMovieComment(userId, commentId);
        return ResponseEntity.ok(APIResponse.builder().success(true).message("movie_comment_deleted").build());
    }

    @GetMapping("/movies/comment/{commentId}")
    public ResponseEntity<MovieCommentDTO> getMovieComment(
            @PathVariable Long commentId
    ) {
        MovieCommentDTO dto = userService.getMovieCommentById(commentId);
        return ResponseEntity.ok(dto);
    }

}
