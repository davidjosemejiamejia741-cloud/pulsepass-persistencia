package com.pulsepass.pulsepass.security;

import static org.springframework.http.HttpMethod.*;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  AuthenticationManager authenticationManager(
      CustomUserDetailsService users, PasswordEncoder encoder) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(encoder);
    return new ProviderManager(provider);
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtService jwt,
      CustomUserDetailsService users,
      SecurityErrors errors,
      RestAuthenticationEntryPoint entry,
      RestAccessDeniedHandler denied,
      @Value("${pulsepass.security.cors-origins:}") List<String> origins,
      @Value("${pulsepass.security.require-https:false}") boolean https)
      throws Exception {
    var cors = new CorsConfiguration();
    cors.setAllowedOrigins(origins);
    cors.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
    cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    cors.setExposedHeaders(List.of("X-Request-ID"));
    cors.setAllowCredentials(false);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    http.csrf(c -> c.disable())
        .cors(c -> c.configurationSource(source))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .requestCache(c -> c.disable())
        .formLogin(c -> c.disable())
        .httpBasic(c -> c.disable())
        .logout(c -> c.disable())
        .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(POST, "/api/auth/login", "/api/users")
                    .permitAll()
                    .requestMatchers(
                        GET,
                        "/api/venues/*",
                        "/api/artists/*",
                        "/api/events/published",
                        "/api/events/by-artist")
                    .permitAll()
                    .requestMatchers(GET, "/api/events/*/tickets/paid")
                    .hasAnyRole("STAFF", "ADMIN")
                    .requestMatchers(GET, "/api/events/*")
                    .permitAll()
                    .requestMatchers(POST, "/api/events", "/api/events/*/artists/*")
                    .hasRole("ADMIN")
                    .requestMatchers(PATCH, "/api/events/*/publish")
                    .hasRole("ADMIN")
                    .requestMatchers(POST, "/api/tickets")
                    .hasRole("USER")
                    .requestMatchers(PATCH, "/api/tickets/*/use")
                    .hasAnyRole("STAFF", "ADMIN")
                    .requestMatchers(PATCH, "/api/tickets/*/cancel")
                    .hasAnyRole("USER", "ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            new JwtAuthenticationFilter(jwt, users, errors),
            UsernamePasswordAuthenticationFilter.class);
    if (https) http.redirectToHttps(c -> {});
    return http.build();
  }
}
