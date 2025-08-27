package com.flagfinder.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.flagfinder.enumeration.Permission.*;
import static com.flagfinder.enumeration.Role.ADMIN;
import static com.flagfinder.enumeration.Role.USER;
import static org.springframework.http.HttpMethod.*;

/**
 * SecurityConfiguration is a configuration class that defines the security settings and filters for the application.
 * It enables web security, method security, and configures the security filter chain.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    /**
     * JWT authentication filter used for authentication.
     */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Beacon authentication filter for handling beacon requests with token parameters.
     */
    private final BeaconAuthenticationFilter beaconAuthFilter;

    /**
     * Authentication provider for authenticating users.
     */
    private final AuthenticationProvider authenticationProvider;

    /**
     * Logout handler for handling user logout.
     */
    private final LogoutHandler logoutHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Value("${spring.frontend.url}")
    private String frontendUrl;

    /**
     * Configures the security filter chain for the application.
     *
     * @param http The HttpSecurity object used to configure the security filters.
     * @return A SecurityFilterChain instance representing the configured security filter chain.
     * @throws Exception If an error occurs during the configuration process.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/ping",
                                "/error",
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html,",
                                "/ws/**",
                                "/ws-native/**"
                        )
                        .permitAll()

                        .requestMatchers("/api/v1/friend-requests").hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers(GET, "/api/v1/friend-requests").hasAnyAuthority(ADMIN_READ.name(), USER_READ.name())
                        .requestMatchers(POST, "/api/v1/friend-requests").hasAnyAuthority(ADMIN_CREATE.name(), USER_CREATE.name())
                        .requestMatchers(DELETE, "/api/v1/friend-requests").hasAnyAuthority(ADMIN_DELETE.name(), USER_DELETE.name())
                        
                        .requestMatchers("/api/v1/register").hasAnyRole(ADMIN.name())
                        .requestMatchers(POST,"/api/v1/register").hasAnyAuthority(ADMIN_CREATE.name())

                        .requestMatchers(DELETE, "/api/v1/users").hasAnyAuthority(ADMIN_DELETE.name())
                        .requestMatchers(PUT, "/api/v1/users").hasAnyAuthority(ADMIN_UPDATE.name(),USER_UPDATE.name())

                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(beaconAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler(
                                (request, response, authentication) -> SecurityContextHolder.clearContext()))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );

        return http.build();
    }

    /**
     * Configures CORS settings for the application.
     *
     * <p>This method defines the allowed origins, HTTP methods, and headers for CORS requests.
     * It also specifies whether credentials (such as cookies or authorization headers) are allowed.</p>
     *
     * @return a {@link CorsConfigurationSource} that provides the CORS configuration for the application.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type","Refresh"));
        configuration.setExposedHeaders(List.of("X-Total-Items", "X-Total-Pages", "X-Current-Page", "Authorization", "Refresh"));
        configuration.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
