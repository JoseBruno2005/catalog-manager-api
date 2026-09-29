package com.catalog.manager.api.controllers;

import com.catalog.manager.api.dto.request.UserRequestDto;
import com.catalog.manager.api.dto.response.UserResponseDto;
import com.catalog.manager.api.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/save")
    public ResponseEntity<UserResponseDto> save(@Valid @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.save(userRequestDto));
    }

}
