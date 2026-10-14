package com.vr.webapp.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * No database for now: users live in this hardcoded list. Swap this out for a
 * repository-backed lookup once real persistence is added.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private record HardcodedUser(String username, String encodedPassword, String[] roles) {
    }

    private final Map<String, HardcodedUser> usersByUsername;

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        List<HardcodedUser> hardcodedUsers = List.of(
            new HardcodedUser("vivekadmin", passwordEncoder.encode("vivekadmin123"), new String[] {"ADMIN", "USER"}),
            new HardcodedUser("vivek", passwordEncoder.encode("vivek123"), new String[] {"USER"})
        );

        this.usersByUsername = hardcodedUsers.stream().collect(Collectors.toMap(HardcodedUser::username, Function.identity()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        HardcodedUser user = usersByUsername.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Spring Security's ProviderManager erases credentials on the returned UserDetails
        // after a successful authentication, so a fresh instance is built on every call
        // rather than returning a cached object (which would end up with a null password).
        return User.withUsername(user.username())
                .password(user.encodedPassword())
                .roles(user.roles())
                .build();
    }
}
