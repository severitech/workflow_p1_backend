package com.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Seguridad deshabilitada para desarrollo.
     * Para prod: reemplazar con JWT + roles por endpoint.
     */
    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {}) // 👈 forma correcta actual
            .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()
        );
        return http.build();
    }

    /** Encoder para contraseñas de usuarios */
    @Bean
    public PasswordEncoder encoderPassword() {
        return new BCryptPasswordEncoder();
    }
}
