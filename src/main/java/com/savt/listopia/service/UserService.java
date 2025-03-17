package com.savt.listopia.service;

import com.savt.listopia.exception.APIException;
import com.savt.listopia.model.movie.Movie;
import com.savt.listopia.model.user.User;
import com.savt.listopia.payload.dto.MovieFrontDTO;
import com.savt.listopia.payload.dto.UserDTO;
import com.savt.listopia.repository.UserRepository;
import com.savt.listopia.security.auth.AuthenticationToken;
import com.savt.listopia.util.PasswordUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return Optional.empty();

        if (authentication instanceof AuthenticationToken authenticationToken) {
            return Optional.of(authenticationToken.getPrincipal().getUserId());
        }

        return Optional.empty();
    }

    public void ChangeUsername(Long userId, String username) {
        if ( userRepository.existsByUsername(username) )
            throw new APIException("username_exists");

        User user = userRepository.findById(userId).orElseThrow();
        user.setUsername(username);
        userRepository.save(user);
    }

    @Transactional
    public List<MovieFrontDTO> getUserLikedMovies(Long userId) {
        List<Movie> movies = userRepository.findLikedMoviesByUserId(userId);
        return movies.stream().map(
                movie -> modelMapper.map(movie, MovieFrontDTO.class)
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

    @Transactional
    public void MakeFriends(Long userId, Long friendId) {
        User user = userRepository.findById(userId).orElseThrow();
        User friend = userRepository.findById(friendId).orElseThrow();
        user.getFriends().add(friend);
        friend.getFriends().add(user);
        userRepository.save(user);
        userRepository.save(friend);
    }

    @Transactional
    public void UserFriendRequest(Long requestOwnerUserId, UUID requestedUserUuid) {
        User requester = userRepository.getReferenceById(requestOwnerUserId);
        User requested = userRepository.getReferenceByUuid(requestedUserUuid);
        requested.getFriendRequests().add(requester);
        userRepository.save(requested);
    }

    @Transactional
    public void AcceptFriend(Long accepterId, UUID acceptedUUID) {
        User accepter = userRepository.getReferenceByUuid(acceptedUUID);
        User accepted = userRepository.getReferenceByUuid(acceptedUUID);
        if ( accepter.getFriendRequests().contains(accepted) ) {
            MakeFriends( accepter.getId(), accepted.getId() );
            accepter.getFriendRequests().remove( accepted );
            userRepository.save(accepter);
        }
    }

    @Transactional
    public List<UserDTO> UserFriendRequests(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getFriendRequests().stream().map(
                requestUser -> modelMapper.map(requestUser, UserDTO.class)
        ).toList();
    }

    @Transactional
    public List<UserDTO> UserFriends(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getFriends().stream().map(
                friend -> modelMapper.map(friend, UserDTO.class)
        ).toList();
    }

}
