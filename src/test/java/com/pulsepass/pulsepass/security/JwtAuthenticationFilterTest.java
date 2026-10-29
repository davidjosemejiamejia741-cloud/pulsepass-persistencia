package com.pulsepass.pulsepass.security;

import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {
  JwtService jwt = mock(JwtService.class);
  CustomUserDetailsService users = mock(CustomUserDetailsService.class);
  SecurityErrors errors = mock(SecurityErrors.class);
  JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, users, errors);
  MockHttpServletRequest req = new MockHttpServletRequest();
  MockHttpServletResponse res = new MockHttpServletResponse();
  FilterChain chain = mock(FilterChain.class);

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void noBearerContinues() throws Exception {
    filter.doFilter(req, res, chain);
    verify(chain).doFilter(req, res);
    verifyNoInteractions(jwt, users);
  }

  @Test
  void malformedStops() throws Exception {
    req.addHeader("Authorization", "Bearer ");
    filter.doFilter(req, res, chain);
    verify(errors).write(req, res, 401);
    verifyNoInteractions(chain);
  }

  @Test
  void duplicateStops() throws Exception {
    req.addHeader("Authorization", "Bearer one");
    req.addHeader("Authorization", "Bearer two");
    filter.doFilter(req, res, chain);
    verify(errors).write(req, res, 401);
    verifyNoInteractions(chain, jwt);
  }

  @Test
  void cannotReplaceAnotherIdentity() throws Exception {
    var prior = new SecurityPrincipal(1L, "one", "one@x.co", "hash", true, List.of("USER"));
    var next = new SecurityPrincipal(2L, "two", "two@x.co", "hash", true, List.of("ADMIN"));
    SecurityContextHolder.getContext()
        .setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(prior, null, prior.getAuthorities()));
    req.addHeader("Authorization", "Bearer token");
    when(jwt.validate("token")).thenReturn(2L);
    when(users.loadById(2L)).thenReturn(next);
    filter.doFilter(req, res, chain);
    verify(errors).write(req, res, 401);
    verifyNoInteractions(chain);
  }

  @Test
  void refreshesSameIdentityRoles() throws Exception {
    var prior = new SecurityPrincipal(1L, "one", "one@x.co", "hash", true, List.of("ADMIN"));
    var next = new SecurityPrincipal(1L, "one", "one@x.co", "hash", true, List.of("USER"));
    SecurityContextHolder.getContext()
        .setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(prior, null, prior.getAuthorities()));
    req.addHeader("Authorization", "Bearer token");
    when(jwt.validate("token")).thenReturn(1L);
    when(users.loadById(1L)).thenReturn(next);
    filter.doFilter(req, res, chain);
    org.assertj.core.api.Assertions.assertThat(
            SecurityContextHolder.getContext().getAuthentication().getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
    verify(chain).doFilter(req, res);
  }
}
