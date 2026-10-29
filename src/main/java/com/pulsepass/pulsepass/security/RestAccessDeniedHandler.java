package com.pulsepass.pulsepass.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler
    implements org.springframework.security.web.access.AccessDeniedHandler {
  private final SecurityErrors errors;

  public RestAccessDeniedHandler(SecurityErrors errors) {
    this.errors = errors;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      org.springframework.security.access.AccessDeniedException exception)
      throws IOException {
    errors.write(request, response, 403);
  }
}
