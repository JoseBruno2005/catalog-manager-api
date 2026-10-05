package com.catalog.manager.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080")
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChainAs (HttpSecurity httpSecurity) {
        var configurer = httpSecurity.apply(new OAuth2AuthorizationServerConfigurer());
        RequestMatcher loginRouter = PathPatternRequestMatcher.withDefaults().matcher("/login");
        var matcher = new OrRequestMatcher(
                configurer.getEndpointsMatcher(), loginRouter
        );
        return httpSecurity.cors(Customizer.withDefaults()).securityMatcher(matcher)
                .authorizeHttpRequests(auth ->
                auth.anyRequest().authenticated()
            ).formLogin(Customizer.withDefaults()).build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChainRS (HttpSecurity httpSecurity) {
        return httpSecurity.cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/save").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                ).oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                ).sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).csrf(
                        csrf -> csrf.disable()
                ).build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.client-id}") String clientId,
            @Value("${app.auth.client-secret}") String clientSecret,
            @Value("${app.auth.redirect-uri}") String redirectUri,
            @Value("${app.auth.refresh-token-days}") long refreshTokenDays

    ) {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                        .refreshTokenTimeToLive(Duration.ofDays(refreshTokenDays))
                        .reuseRefreshTokens(false)
                        .build())
                .redirectUri(redirectUri)
                .scope("products:read")
                .scope("products:write")
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .build();

        return new InMemoryRegisteredClientRepository(client);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:4200");
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
