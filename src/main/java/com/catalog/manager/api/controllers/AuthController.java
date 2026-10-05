package com.catalog.manager.api.controllers;

import com.catalog.manager.api.dto.response.AccessTokenResponse;
import com.catalog.manager.api.dto.request.CodeExchangeRequest;
import com.catalog.manager.api.dto.response.TokenResponse;
import com.catalog.manager.api.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refresh_token";

    private final AuthService authService;
    private final boolean cookieSecure;
    private final Duration refreshTtl;

    public AuthController(
            AuthService authService,
            @Value("${app.auth.cookie-secure}")
            boolean cookieSecure,
            @Value("${app.auth.refresh-token-days}")
            Long refreshTtl
    ) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
        this.refreshTtl = Duration.ofDays(refreshTtl);
    }

    @PostMapping("/token")
    public ResponseEntity<AccessTokenResponse> exchange(
            @Valid @RequestBody CodeExchangeRequest request
            )
    {
        return withRefreshCookie(authService.exchangeCode(
                request.code(), request.codeVerifier()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @RequestHeader("X-Requested-With") String requestedWith,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken
    ){
        if(refreshToken == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return withRefreshCookie(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("X-Requested-With") String requestedWith,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken
    ){
        if(refreshToken != null){
            authService.revoke(refreshToken);
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, buildCookie("", Duration.ZERO).toString())
                .build();
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Void> handleTokenError() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, buildCookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseEntity<AccessTokenResponse> withRefreshCookie(TokenResponse tokens) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        buildCookie(tokens.refreshToken(), refreshTtl).toString())
                .body(new AccessTokenResponse(tokens.accessToken(), tokens.expiresIn()));
    }

    private ResponseCookie buildCookie(String value, Duration maxAge){
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(maxAge)
                .build();
    }

}
