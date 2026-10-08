package com.catalog.manager.api.services;

import com.catalog.manager.api.dto.response.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class AuthService {
    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public AuthService(
            @Value("${app.auth.server-url}")
            String serverUrl,
            @Value("${app.auth.client-id}")
            String clientId,
            @Value("${app.auth.client-secret}")
            String clientSecret,
            @Value("${app.auth.redirect-uri}")
            String redirectUri
    ) {
        this.restClient = RestClient.builder().baseUrl(serverUrl).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public TokenResponse exchangeCode(String code, String codeVerifier){
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("code_verifier", codeVerifier);
        return requestToken(form);
    }

    public TokenResponse refresh(String refreshToken){
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        return  requestToken(form);
    }

    public void revoke(String refreshToken){
        var form = new LinkedMultiValueMap<String, String>();
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");
        restClient.post()
                .uri("/oauth2/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }

    private TokenResponse requestToken(MultiValueMap<String, String> form){
        return restClient.post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
    }
}
