package com.pulsepass.pulsepass.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwt;
  private final CustomUserDetailsService users;
  private final SecurityErrors errors;

  public JwtAuthenticationFilter(JwtService jwt, CustomUserDetailsService users, SecurityErrors errors) {
    this.jwt = jwt;
    this.users = users;
    this.errors = errors;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
  throws ServletException, IOException {

    String requestId = UUID.randomUUID().toString();
    MDC.put("requestId", requestId);
    response.setHeader("X-Request-ID", requestId);
    try {
      var headers = Collections.list(request.getHeaders("Authorization"));
      if (!headers.isEmpty()) {
        try {
          if (headers.size() != 1 || !headers.getFirst().matches("(?i)Bearer [^ ,\\s]+"))
          throw new IllegalArgumentException();

          var principal = users.loadById(jwt.validate(headers.getFirst().substring(7)));
          if (!principal.isEnabled()) throw new IllegalArgumentException();

          var current = SecurityContextHolder.getContext().getAuthentication();
          if (current != null && current.isAuthenticated() && (!(current.getPrincipal() instanceof SecurityPrincipal old)
                || !old.userId().equals(principal.userId())))
          throw new IllegalArgumentException();

          principal.eraseCredentials();
          var context = SecurityContextHolder.createEmptyContext();
          context.setAuthentication(
              UsernamePasswordAuthenticationToken.authenticated(
                  principal, null, principal.getAuthorities()));
          SecurityContextHolder.setContext(context);

        } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
          SecurityContextHolder.clearContext();
          errors.write(request, response, 401);
          return;
        }
      }
      chain.doFilter(request, response);
    } finally {
      MDC.remove("requestId");
    }
  }
}
