package com.savt.listopia.service;

import com.savt.listopia.model.user.User;
import com.savt.listopia.repository.UserRepository;
import com.savt.listopia.security.auth.AuthenticationToken;
import com.savt.listopia.util.PasswordUtil;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

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
        User user = userRepository.findByEmail(email);
        if (verifyUserPassword(user, plainPassword))
            return user;

        return null;
    }

    public boolean verifyUserPassword(User user, String enteredPassword) {
        if ( user == null )
            return false;

        return PasswordUtil.verifyPassword(enteredPassword, user.getHashedPassword());
    }

    public void changeUserPassword(User user, String newPassword) {
        String password = PasswordUtil.hashPassword(newPassword);
        user.setHashedPassword(password);
        userRepository.save(user);
    }

    public boolean changeUserUsername(User user, String newUsername) {
        if (userRepository.existsByUsername(newUsername))
            return false; // Username already taken

        user.setUsername(newUsername);
        userRepository.save(user);
        return true;
    }

    public boolean changeUserEmail(User user, String newEmail) {
        if (userRepository.existsByEmail(newEmail))
            return false; // Email already taken

        user.setEmail(newEmail);
        userRepository.save(user);
        return true;
    }

    public User getUserByUuid(UUID uuid) {
        return userRepository.findByUuid(uuid);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return null;

        if (authentication instanceof AuthenticationToken authenticationToken) {
            return getUserById(authenticationToken.getPrincipal().getUserId());
        }

        return null;
    }
}
