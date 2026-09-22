package com.yeab.ticketing.notification.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
@Configuration @EnableMethodSecurity public class SecurityConfig {
 private final boolean enabled; public SecurityConfig(@Value("${app.security.enabled:false}") boolean enabled) { this.enabled = enabled; }
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { http.csrf(AbstractHttpConfigurer::disable).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a -> { a.requestMatchers("/actuator/health", "/actuator/info").permitAll(); if (enabled) a.anyRequest().authenticated(); else a.anyRequest().permitAll(); }); if (enabled) http.oauth2ResourceServer(o -> o.jwt(j -> { })); return http.build(); }
}
