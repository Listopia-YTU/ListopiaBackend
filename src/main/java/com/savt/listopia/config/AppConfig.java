package com.savt.listopia.config;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.people.Person;
import com.savt.listopia.payload.dto.MovieDTO;
import info.movito.themoviedbapi.model.people.PersonDb;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
