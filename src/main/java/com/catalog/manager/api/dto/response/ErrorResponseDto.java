package com.catalog.manager.api.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ErrorResponseDto {
    @NotBlank
    @NotNull
    private LocalDateTime timesTamp;
    @NotNull
    private Integer statusCode;
    @NotBlank
    @NotNull
    private String message;
}
