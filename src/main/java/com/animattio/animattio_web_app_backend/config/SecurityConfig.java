package com.animattio.animattio_web_app_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * Security configuration class for the application.
 * This class sets up security rules, such as CORS, CSRF protection, role-based access control,
 * session management, and integration of a custom authentication filter.
 */
@Configuration
public class SecurityConfig {

    private final FirebaseAuthFilter firebaseAuthFilter;

    /**
     * Constructor to inject the FirebaseAuthFilter dependency.
     *
     * @param firebaseAuthFilter the custom filter for Firebase authentication.
     */
    public SecurityConfig(FirebaseAuthFilter firebaseAuthFilter) {
        this.firebaseAuthFilter = firebaseAuthFilter;
    }

    /**
     * Configures the security filter chain, including CORS, CSRF protection, authorization rules,
     * session management, and integration of the custom FirebaseAuthFilter.
     *
     * @param http the {@link HttpSecurity} object to configure.
     * @return the configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS settings
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("https://frontend-animattio-39d2470d8e0c.herokuapp.com"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))

                .csrf(csrf -> csrf.disable())

                // Authorization for endpoints
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(HttpMethod.OPTIONS, "/patients/**").permitAll()
                                .requestMatchers("/patients/**").hasRole("doctor")
                                .requestMatchers("/tests/**").hasRole("doctor")
                                .requestMatchers("/doctors/create-doctor").hasRole("admin")
                                .requestMatchers("/doctors/delete-doctor").hasRole("admin")
                                .requestMatchers("/doctors/get-doctor-list").hasRole("admin")
                                .requestMatchers("/doctors/get-doctor").permitAll()
                                .requestMatchers("/doctors/username/**").permitAll()
                                .requestMatchers("/doctors/update-profile/**").permitAll()
                                .requestMatchers("doctors/doctor-exists/**").hasRole("admin")
                                .requestMatchers("/signin").permitAll()
                                .requestMatchers("doctors/delete-by-username").hasRole("admin")
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Each request must contain all the necessary
                        // authentication details like token
                );

        http.addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class); // to validate the token

        return http.build();
    }
}
