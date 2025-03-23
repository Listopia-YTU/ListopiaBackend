package com.savt.listopia.controller;

import com.savt.listopia.payload.APIResponse;
import com.savt.listopia.payload.dto.MovieTranslationDTO;
import com.savt.listopia.util.FetchUtil;
import info.movito.themoviedbapi.tools.TmdbException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class FetchController {
    @Autowired
    private FetchUtil fetchUtil;

    @PostMapping("/fetch/genres")
    public ResponseEntity<String> fetchGenres() throws TmdbException {
        fetchUtil.fetchGenres();
        return new ResponseEntity<>("Fetched genres", HttpStatus.CREATED);
    }

    @PostMapping("/fetch/genres/translations")
    public ResponseEntity<String> fetchGenreTranslation(@RequestParam(name = "language") String language) throws TmdbException {
        fetchUtil.fetchGenreTranslations(language);
        return new ResponseEntity<>("Fetched genre translations for language: " + language, HttpStatus.CREATED);
    }

}
