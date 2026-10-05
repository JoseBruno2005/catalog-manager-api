package com.catalog.manager.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CodeExchangeRequest(
        @NotBlank String code,
        @NotBlank String codeVerifier
) {
}
