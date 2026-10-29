package com.pulsepass.pulsepass.security;

import com.pulsepass.pulsepass.dto.response.ErrorResponse;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SecurityErrors {
  private final ObjectMapper mapper;

  public SecurityErrors(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public void write(HttpServletRequest request, HttpServletResponse response, int status)
      throws IOException {
    LoggerFactory.getLogger(SecurityErrors.class)
        .info("security_denied status={} requestId={}", status, MDC.get("requestId"));
    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    if (status == 401) response.setHeader("WWW-Authenticate", "Bearer");
    mapper.writeValue(
        response.getOutputStream(),
        new ErrorResponse(
            LocalDateTime.now(),
            status,
            status == 401 ? "Unauthorized" : "Forbidden",
            status == 401
                ? "Credenciales inválidas o autenticación requerida"
                : "No tiene permisos para realizar esta operación",
            Map.of()));
  }
}
