package com.savt.listopia.controller;

import com.savt.listopia.config.AppConstants;
import com.savt.listopia.payload.APIResponse;
import com.savt.listopia.payload.dto.MovieCommentDTO;
import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.dto.MovieTranslationDTO;
import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.payload.response.MovieResponse;
import com.savt.listopia.service.MovieService;
import com.savt.listopia.service.StatService;
import com.savt.listopia.service.UserService;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/stats")
@Validated
public class StatController {
    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/movies/most-liked")
    public ResponseEntity<MovieFrontResponse> getMostLikedMovies( @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                             @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                             @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
                                                             @RequestParam(name = "language", defaultValue = "en", required = false) String language) {
        MovieFrontResponse movieResponse = statService.getMostLikedMovies(pageNumber, pageSize, sortOrder, language);
        return new ResponseEntity<>(movieResponse, HttpStatus.OK);
    }


}
