package com.pulsepass.pulsepass.security;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final JdbcTemplate jdbc;

  public CustomUserDetailsService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public SecurityPrincipal loadUserByUsername(String identifier) {
    return load("(u.username = ? OR lower(u.email) = lower(?))", identifier, identifier);
  }

  public SecurityPrincipal loadById(Long id) {
    return load("u.id = ?", id);
  }

  private SecurityPrincipal load(String where, Object... args) {
    // One query: no lazy role collection and no N+1. Ambiguous identifiers fail closed.
    var rows =
        jdbc.queryForList(
            "SELECT u.id,u.username,u.email,u.active,c.password_hash,r.role FROM users u JOIN"
                + " user_credentials c ON c.user_id=u.id JOIN user_roles r ON r.user_id=u.id WHERE "
                + where,
            args);
    if (rows.isEmpty() || rows.stream().map(r -> r.get("id")).distinct().count() != 1)
      throw new UsernameNotFoundException("Credenciales inválidas");
    var u = rows.getFirst();
    return new SecurityPrincipal(
        ((Number) u.get("id")).longValue(),
        (String) u.get("username"),
        (String) u.get("email"),
        (String) u.get("password_hash"),
        (Boolean) u.get("active"),
        rows.stream().map(r -> (String) r.get("role")).toList());
  }
}
