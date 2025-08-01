package com.flagfinder.config;


import com.flagfinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ApplicationConfig is a configuration class that defines the beans and dependencies required for user authentication
 * and authorization within the application.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    /**
     * Repository used to retrieve user data.
     */
    private final UserRepository repository;

    /**
     * Creates a bean for the UserDetailsService interface, which retrieves user details based on their email.
     *
     * @return An implementation of UserDetailsService.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Creates a bean for the AuthenticationProvider interface, which performs authentication based on user details.
     *
     * @return An instance of AuthenticationProvider.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Creates a bean for the AuthenticationManager interface, which handles the authentication process.
     *
     * @param config The AuthenticationConfiguration instance used to obtain the AuthenticationManager.
     * @return An implementation of AuthenticationManager.
     * @throws Exception If an error occurs while retrieving the AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Creates a bean for the PasswordEncoder interface, which is used to encode and verify passwords.
     *
     * @return An instance of PasswordEncoder (BCryptPasswordEncoder in this case).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
