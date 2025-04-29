package com.savt.listopia.service;

import com.savt.listopia.exception.userException.UserNotAuthorizedException;
import com.savt.listopia.model.user.User;
import com.savt.listopia.model.user.UserRole;
import com.savt.listopia.repository.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;

    public AuthServiceImpl(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void requireRoleOrThrow(UserRole role) {
        Long userId = userService.getCurrentUserIdOrThrow();
        User user = userRepository.findById(userId).orElseThrow(UserNotAuthorizedException::new);
        if ( !user.getRole().hasAtLeast(role) ) {
            throw new UserNotAuthorizedException();
        }
    }
}
