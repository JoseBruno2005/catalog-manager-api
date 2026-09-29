package com.catalog.manager.api.services;

import com.catalog.manager.api.domain.User;
import com.catalog.manager.api.domain.UserDetailsAdapter;
import com.catalog.manager.api.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetailsAdapter loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailUser(username);
        if(user == null){
            throw new UsernameNotFoundException("Usuário não encontrado.");
        }

        return UserDetailsAdapter.builder().user(user).build();
    }
}
