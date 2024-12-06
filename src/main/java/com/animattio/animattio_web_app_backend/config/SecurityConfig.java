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

@Configuration
public class SecurityConfig {

    private final FirebaseAuthFilter firebaseAuthFilter;

    public SecurityConfig(FirebaseAuthFilter firebaseAuthFilter) {
        this.firebaseAuthFilter = firebaseAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("https://frontend-animattio-39d2470d8e0c.herokuapp.com"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))
                .csrf(csrf -> csrf.disable())
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
//                                .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
