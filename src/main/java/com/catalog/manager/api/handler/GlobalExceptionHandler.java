package com.catalog.manager.api.handler;

import com.catalog.manager.api.exceptions.EmailAlreadyExistsException;
import com.catalog.manager.api.dto.response.ErrorResponseDto;
import com.catalog.manager.api.exceptions.UsernameNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseDto.builder()
                        .timesTamp(LocalDateTime.now())
                        .statusCode(HttpStatus.CONFLICT.value())
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> usernameNotFoundException(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDto.builder()
                        .timesTamp(LocalDateTime.now())
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDto.builder()
                        .timesTamp(LocalDateTime.now())
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(ex.getMessage())
                        .build());
    }

}
