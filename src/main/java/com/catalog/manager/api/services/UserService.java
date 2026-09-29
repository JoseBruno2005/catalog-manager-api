package com.catalog.manager.api.services;

import com.catalog.manager.api.exceptions.EmailAlreadyExistsException;
import com.catalog.manager.api.domain.User;
import com.catalog.manager.api.domain.enums.UserRole;
import com.catalog.manager.api.dto.request.UserRequestDto;
import com.catalog.manager.api.dto.response.UserResponseDto;
import com.catalog.manager.api.mapper.UserMapper;
import com.catalog.manager.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto save(UserRequestDto userRequestDto) {
        User user = userMapper.dtoToEntity(userRequestDto);
        user.setRole(UserRole.CUSTOMER);

        if(Boolean.TRUE.equals(userRepository.existsByEmail(user.getEmail()))){
            throw new EmailAlreadyExistsException("Email já cadastrado.");
        }

        String hash = passwordEncoder.encode(user.getPassword());
        user.setPassword(hash);

        return userMapper.entityToDto(userRepository.save(user));
    }

}
