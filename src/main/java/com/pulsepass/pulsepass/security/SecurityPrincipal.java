package com.pulsepass.pulsepass.security;

import java.util.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class SecurityPrincipal extends org.springframework.security.core.userdetails.User {
  private final Long userId;
  private final String email;

  public SecurityPrincipal(
      Long id,
      String username,
      String email,
      String hash,
      boolean active,
      Collection<String> roles) {
    super(
        username,
        hash,
        active,
        true,
        true,
        true,
        roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
    this.userId = id;
    this.email = email;
  }

  public Long userId() {
    return userId;
  }

  public String email() {
    return email;
  }
}
