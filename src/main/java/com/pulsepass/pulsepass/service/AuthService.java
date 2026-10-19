package com.pulsepass.pulsepass.service;

import com.pulsepass.pulsepass.dto.request.*;
import com.pulsepass.pulsepass.dto.response.*;
import com.pulsepass.pulsepass.security.*;
import org.slf4j.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserService users;
  private final JdbcTemplate jdbc;
  private final PasswordEncoder encoder;
  private final AuthenticationManager manager;
  private final JwtService jwt;

  public AuthService(
      UserService users,
      JdbcTemplate jdbc,
      PasswordEncoder encoder,
      AuthenticationManager manager,
      JwtService jwt) {
    this.users = users;
    this.jdbc = jdbc;
    this.encoder = encoder;
    this.manager = manager;
    this.jwt = jwt;
  }

  @Transactional
  public UserResponse register(RegisterUserRequest request) {
    if (!new PasswordValidator().isValid(request.password(), null))
      throw new IllegalArgumentException("Invalid password length");
    var user = users.register(request);
    jdbc.update(
        "INSERT INTO user_credentials(user_id,password_hash) VALUES (?,?)",
        user.id(),
        encoder.encode(request.password()));
    jdbc.update("INSERT INTO user_roles(user_id,role) VALUES (?, 'USER')", user.id());
    return user;
  }

  public AuthResponse login(LoginRequest request) {
    try {
      if (request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
        throw new BadCredentialsException("Credenciales inválidas");
      var auth =
          manager.authenticate(
              UsernamePasswordAuthenticationToken.unauthenticated(
                  request.identifier(), request.password()));
      var principal = (SecurityPrincipal) auth.getPrincipal();
      LoggerFactory.getLogger(AuthService.class)
          .info("login_success requestId={}", MDC.get("requestId"));
      return new AuthResponse(jwt.issue(principal.userId()), "Bearer", jwt.expiresIn());
    } catch (AuthenticationException e) {
      LoggerFactory.getLogger(AuthService.class)
          .info("login_failure requestId={}", MDC.get("requestId"));
      throw new BadCredentialsException("Credenciales inválidas");
    }
  }
}
