package com.pulsepass.pulsepass.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pulsepass.pulsepass.dto.request.*;
import com.pulsepass.pulsepass.service.*;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
  AuthenticationManager manager = mock(AuthenticationManager.class);
  JwtService jwt = mock(JwtService.class);
  AuthService service =
      new AuthService(
          mock(UserService.class),
          mock(JdbcTemplate.class),
          mock(PasswordEncoder.class),
          manager,
          jwt);

  @Test
  void successfulLogin() {
    var p = new SecurityPrincipal(1L, "a", "a@b.co", "hash", true, List.of("USER"));
    when(manager.authenticate(any()))
        .thenReturn(UsernamePasswordAuthenticationToken.authenticated(p, null, p.getAuthorities()));
    when(jwt.issue(1L)).thenReturn("token");
    when(jwt.expiresIn()).thenReturn(900L);
    assertThat(service.login(new LoginRequest("a", "password")).accessToken()).isEqualTo("token");
  }

  @Test
  void wrongPassword() {
    when(manager.authenticate(any())).thenThrow(new BadCredentialsException("internal"));
    assertThatThrownBy(() -> service.login(new LoginRequest("a", "bad")))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Credenciales inválidas");
    verifyNoInteractions(jwt);
  }

  @Test
  void inactive() {
    when(manager.authenticate(any())).thenThrow(new DisabledException("internal"));
    assertThatThrownBy(() -> service.login(new LoginRequest("a", "password")))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Credenciales inválidas");
    verifyNoInteractions(jwt);
  }
}
