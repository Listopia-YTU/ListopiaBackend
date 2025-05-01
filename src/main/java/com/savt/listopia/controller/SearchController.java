package com.savt.listopia.controller;

import com.savt.listopia.model.Category;
import com.savt.listopia.payload.dto.UserDTO;
import com.savt.listopia.payload.dto.movie.MovieFrontDTO;
import com.savt.listopia.payload.dto.search.QueryResultDTO;
import com.savt.listopia.service.UserService;
import com.savt.listopia.service.movie.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final Map<Category, BiConsumer<QueryResultDTO, String>> categoryHandlers = Map.of(
            Category.USERS, this::handleUsers,
            Category.MOVIES, this::handleMovies
    );
    private final UserService userService;
    private final MovieService movieService;

    public SearchController(UserService userService, MovieService movieService) {
        this.userService = userService;
        this.movieService = movieService;
    }

    private void handleUsers(QueryResultDTO result, String query) {
        Page<UserDTO> users = userService.searchUsers(query, 0, 15);
        result.getResults().put(Category.USERS, users);
    }

    private void handleMovies(QueryResultDTO result, String query) {
        Page<MovieFrontDTO> movies = movieService.searchByTitle(query, 0, 15);
        result.getResults().put(Category.MOVIES, movies);
    }

    @GetMapping("/")
    public ResponseEntity<QueryResultDTO> index(
            @RequestParam String query,
            @RequestParam List<Category> categories // Kategoriler isteğe bağlı
    ) {
        if ( categories.contains(Category.ALL) ) {
            categories = Category.all();
        }

        QueryResultDTO queryResultDTO = new QueryResultDTO();

        for ( Category category : categories ) {
            BiConsumer<QueryResultDTO, String> handler = categoryHandlers.get(category);
            if (handler != null) {
                handler.accept(queryResultDTO, query);
            }
        }

        return ResponseEntity.ok(queryResultDTO);
    }

}
