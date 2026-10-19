package com.pulsepass.pulsepass.service;

import com.pulsepass.pulsepass.repository.*;
import com.pulsepass.pulsepass.security.SecurityPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("authorizationService")
public class AuthorizationService {
  private final UserRepository users;
  private final TicketRepository tickets;

  public AuthorizationService(UserRepository users, TicketRepository tickets) {
    this.users = users;
    this.tickets = tickets;
  }

  public static boolean privileged() {
    return role("STAFF") || role("ADMIN");
  }

  public static boolean role(String role) {
    var a = SecurityContextHolder.getContext().getAuthentication();
    return a != null
        && a.isAuthenticated()
        && a.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_" + role));
  }

  private boolean owns(Long id) {
    var a = SecurityContextHolder.getContext().getAuthentication();
    return a != null && a.getPrincipal() instanceof SecurityPrincipal p && p.userId().equals(id);
  }

  public boolean email(String email, boolean admin) {
    return (admin && role("ADMIN"))
        || users.findByEmailIgnoreCase(email).map(u -> owns(u.getId())).orElse(false);
  }

  public boolean username(String username) {
    return role("ADMIN") || users.findByUsername(username).map(u -> owns(u.getId())).orElse(false);
  }

  public boolean ticket(String code, boolean read) {
    return role("ADMIN")
        || (read && role("STAFF"))
        || (role("USER")
            && tickets.findByTicketCode(code).map(t -> owns(t.getUser().getId())).orElse(false));
  }
}
