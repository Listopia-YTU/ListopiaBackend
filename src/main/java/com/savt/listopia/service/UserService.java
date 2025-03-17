package com.savt.listopia.service;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.User;
import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.dto.UserDTO;
import com.savt.listopia.repository.MovieRepository;
import com.savt.listopia.repository.UserRepository;
import com.savt.listopia.security.auth.AuthenticationToken;
import com.savt.listopia.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    private MovieRepository movieRepository;

    public User registerUser(String firstname, String lastName, String email, String username, String plainPassword) {
        if (userRepository.existsByUsername(username))
            return null;

        if (userRepository.existsByEmail(email))
            return null;

        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstname);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setHashedPassword(hashedPassword);

        userRepository.save(user);
        return user;
    }

    public User getUserByEmailPassword(String email, String plainPassword) {
        User user = userRepository.findByEmail(email).orElseThrow();
        if (verifyUserPassword(user, plainPassword))
            return user;

        return null;
    }

    public boolean verifyUserPassword(User user, String enteredPassword) {
        if (user == null)
            return false;

        return PasswordUtil.verifyPassword(enteredPassword, user.getHashedPassword());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        return modelMapper.map(user, UserDTO.class);
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return null;

        if (authentication instanceof AuthenticationToken authenticationToken) {
            return authenticationToken.getPrincipal().getUserId();
        }

        return null;
    }

    @Transactional
    public List<MovieDTO> getUserLikedMovies(Long userId) {
        List<Movie> movies = userRepository.findLikedMoviesByUserId(userId); // Convert to ArrayList
        return movies.stream().map(
                movie -> modelMapper.map(movie, MovieDTO.class)
        ).toList();
    }

    @Transactional
    public void likeMovie(Long userId, Movie movie, Boolean liked) {
        User user = userRepository.findById(userId).orElseThrow();
        if ( liked )
            user.getLikedMovies().add(movie);
        else
            user.getLikedMovies().remove(movie);
        userRepository.save(user);
    }

}
