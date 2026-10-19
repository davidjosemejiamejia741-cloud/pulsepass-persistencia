package com.pulsepass.pulsepass.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class CustomUserDetailsServiceTest {
  JdbcTemplate jdbc = mock(JdbcTemplate.class);
  CustomUserDetailsService service = new CustomUserDetailsService(jdbc);

  Map<String, Object> row(boolean active, String role) {
    return Map.of(
        "id",
        1L,
        "username",
        "a",
        "email",
        "a@b.co",
        "password_hash",
        "hash",
        "active",
        active,
        "role",
        role);
  }

  @Test
  void currentRolesAndStatus() {
    when(jdbc.queryForList(anyString(), eq(1L)))
        .thenReturn(List.of(row(true, "ADMIN")), List.of(row(false, "USER")));
    assertThat(service.loadById(1L).getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_ADMIN");
    var next = service.loadById(1L);
    assertThat(next.isEnabled()).isFalse();
    assertThat(next.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    verify(jdbc, times(2)).queryForList(anyString(), eq(1L));
  }

  @Test
  void unknown() {
    when(jdbc.queryForList(anyString(), eq(1L))).thenReturn(List.of());
    assertThatThrownBy(() -> service.loadById(1L)).isInstanceOf(UsernameNotFoundException.class);
  }
}
