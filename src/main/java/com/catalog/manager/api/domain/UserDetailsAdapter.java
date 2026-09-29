package com.catalog.manager.api.domain;

import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
public class UserDetailsAdapter implements UserDetails {
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String userRole = this.user.getRole().toString();

        String role = "ROLE_" + userRole;
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        return List.of(authority);
    }

    @Override
    public @Nullable String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getEmail();
    }
}
