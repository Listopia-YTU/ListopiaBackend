package com.savt.listopia.service;

import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.User;
import com.savt.listopia.payload.dto.MovieDTO;
import com.savt.listopia.payload.dto.UserDTO;
import com.savt.listopia.repository.UserRepository;
import com.savt.listopia.security.auth.AuthenticationToken;
import com.savt.listopia.util.PasswordUtil;

import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ModelMapper modelMapper;

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

    public List<Movie> getUserLikedMovies(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Movie> likedMovies = user.getLikedMovies();
        return likedMovies;
    }
}
