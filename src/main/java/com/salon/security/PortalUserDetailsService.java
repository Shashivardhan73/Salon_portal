package com.salon.security;

import com.salon.repository.AppUserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PortalUserDetailsService implements UserDetailsService {
    private final AppUserRepository users;

    public PortalUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var account = users.findByUsername(username.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }
}
