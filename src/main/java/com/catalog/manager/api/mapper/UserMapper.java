package com.catalog.manager.api.mapper;

import com.catalog.manager.api.domain.User;
import com.catalog.manager.api.dto.request.UserRequestDto;
import com.catalog.manager.api.dto.response.UserResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User dtoToEntity(UserRequestDto userRequestDto);
    UserResponseDto entityToDto(User user);
}
