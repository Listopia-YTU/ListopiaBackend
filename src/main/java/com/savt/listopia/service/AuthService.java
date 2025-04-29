package com.savt.listopia.service;

import com.savt.listopia.model.user.UserRole;

public interface AuthService {
    void requireRoleOrThrow(UserRole role);
}
