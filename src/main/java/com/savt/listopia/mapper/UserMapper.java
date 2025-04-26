package com.savt.listopia.mapper;

import com.savt.listopia.model.user.User;
import com.savt.listopia.payload.dto.UserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
}
