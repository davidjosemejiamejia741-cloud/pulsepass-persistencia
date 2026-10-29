package com.pulsepass.pulsepass.security;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;

@TestConfiguration
@Import({
  SecurityConfig.class,
  SecurityErrors.class,
  RestAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class
})
public class MvcSecuritySupport {
  @Bean
  JwtService jwtService() {
    return mock(JwtService.class);
  }

  @Bean
  CustomUserDetailsService customUserDetailsService() {
    return mock(CustomUserDetailsService.class);
  }
}
