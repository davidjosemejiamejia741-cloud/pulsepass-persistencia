package com.pulsepass.pulsepass.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint
    implements org.springframework.security.web.AuthenticationEntryPoint {
  private final SecurityErrors errors;

  public RestAuthenticationEntryPoint(SecurityErrors errors) {
    this.errors = errors;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      org.springframework.security.core.AuthenticationException exception)
      throws IOException {
    errors.write(request, response, 401);
  }
}
