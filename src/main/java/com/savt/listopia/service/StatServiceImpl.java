package com.savt.listopia.service;

import com.savt.listopia.payload.response.MovieFrontResponse;
import com.savt.listopia.payload.response.MovieResponse;
import com.savt.listopia.repository.MovieRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatServiceImpl implements StatService{

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieServiceImpl movieServiceImpl;

    @Override
    public MovieFrontResponse getMostLikedMovies(Integer pageNumber, Integer pageSize, String sortOrder, String language) {
        return movieServiceImpl.getFrontMovies(pageNumber, pageSize, "likeCount", sortOrder, null, language);
    }
}
