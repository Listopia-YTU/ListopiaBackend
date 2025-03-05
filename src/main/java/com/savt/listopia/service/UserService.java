package com.savt.listopia.service;

import com.savt.listopia.model.user.User;
import com.savt.listopia.repository.UserRepository;
import com.savt.listopia.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public void registerUser(User user, String plainPassword) {
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        user.setHashedPassword(hashedPassword);
        userRepository.save(user);
    }

    public boolean verifyUserPassword(User user, String enteredPassword) {
        return PasswordUtil.verifyPassword(enteredPassword, user.getHashedPassword());
    }

    public void changeUserPassword(User user, String newPassword) {
        String password = PasswordUtil.hashPassword(newPassword);
        user.setHashedPassword(password);
        userRepository.save(user);
    }

    public boolean changeUserUsername(User user, String newUsername) {
        if ( userRepository.existsByUsername(newUsername) )
            return false; // Username already taken

        user.setUsername(newUsername);
        userRepository.save(user);
        return true;
    }

    public boolean changeUserEmail(User user, String newEmail) {
        if ( userRepository.existsByEmail(newEmail) )
            return false; // Email already taken

        user.setEmail(newEmail);
        userRepository.save(user);
        return true;
    }

}
