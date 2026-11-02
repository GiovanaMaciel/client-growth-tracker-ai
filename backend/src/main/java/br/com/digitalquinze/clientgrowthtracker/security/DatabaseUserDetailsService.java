package br.com.digitalquinze.clientgrowthtracker.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.digitalquinze.clientgrowthtracker.user.AppUser;
import br.com.digitalquinze.clientgrowthtracker.user.AppUserRepository;

@Service
public class DatabaseUserDetailsService
        implements UserDetailsService {

    private final AppUserRepository userRepository;

    public DatabaseUserDetailsService(
            AppUserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AppUser user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(
                    () -> new UsernameNotFoundException(
                        "User not found"
                    )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(
                    "ROLE_" + user.getRole().name()
                )
                .disabled(!user.isActive())
                .build();
    }
}