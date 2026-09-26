package com.yeab.ticketing.venue.config;

import com.yeab.ticketing.common.docs.OpenApiDocsPaths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final boolean securityEnabled;

    public SecurityConfig(@Value("${app.security.enabled:false}") boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/actuator/health", "/actuator/info").permitAll();
                    authorize.requestMatchers(OpenApiDocsPaths.publicApiDocsPaths()).permitAll();
                    authorize.requestMatchers(GET, "/api/venues/**").permitAll();
                    if (securityEnabled) {
                        authorize.requestMatchers("/api/venues/**").hasAnyRole("OPERATOR", "ADMIN");
                        authorize.anyRequest().authenticated();
                    } else {
                        authorize.anyRequest().permitAll();
                    }
                });
        if (securityEnabled) http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> { }));
        return http.build();
    }
}
