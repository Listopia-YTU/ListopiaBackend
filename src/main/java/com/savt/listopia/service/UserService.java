package com.savt.listopia.service;

import com.savt.listopia.model.user.*;
import com.savt.listopia.payload.dto.*;

import java.util.Optional;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserDTO getUserByUsername(String username);
    User registerUser(String firstname, String lastName, String email, String username, String plainPassword);
    User getUserByEmailPassword(String email, String plainPassword);
    boolean verifyUserPassword(User user, String enteredPassword);
    UserDTO getUserById(Long id);
    Optional<Long> getCurrentUserId();
    Long getCurrentUserIdOrThrow();
    void ChangeUsername(Long userId, String username);
    Long getUserIdFromUUID(UUID uuid);
    void changePassword(Long userId, String password);
    void changeBiography(Long userId, String biography);

    Page<UserActivityDTO> getUserActivities(Long userId, int pageNumber, int pageSize);
    Page<NotificationDTO> getUserNotifications(Long userId, Integer pageNumber, Integer pageSize);
    void userNotifiedBefore(Long userId, Long timestamp);
}
