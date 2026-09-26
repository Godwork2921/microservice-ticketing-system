package com.yeab.ticketing.reservation.config;

import com.yeab.ticketing.common.docs.OpenApiDocsPaths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final boolean enabled;
    public SecurityConfig(@Value("${app.security.enabled:false}") boolean enabled) { this.enabled = enabled; }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator/health", "/actuator/info").permitAll();
                    auth.requestMatchers(OpenApiDocsPaths.publicApiDocsPaths()).permitAll();
                    if (enabled) auth.anyRequest().authenticated(); else auth.anyRequest().permitAll();
                });
        if (enabled) http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> { }));
        return http.build();
    }
}
