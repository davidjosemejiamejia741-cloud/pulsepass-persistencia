package com.pulsepass.pulsepass.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.repository.*;
import com.pulsepass.pulsepass.service.AuthorizationService;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthorizationServiceTest {
  UserRepository users = mock(UserRepository.class);
  TicketRepository tickets = mock(TicketRepository.class);
  AuthorizationService policy = new AuthorizationService(users, tickets);

  void login(String role) {
    var p = new SecurityPrincipal(1L, "andrea", "a@x.test", "hash", true, List.of(role));
    SecurityContextHolder.getContext()
        .setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(p, null, p.getAuthorities()));
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void ownerByImmutableId() {
    login("USER");
    User own = new User();
    own.setId(1L);
    User other = new User();
    other.setId(2L);
    when(users.findByEmailIgnoreCase("own")).thenReturn(Optional.of(own));
    when(users.findByEmailIgnoreCase("other")).thenReturn(Optional.of(other));
    assertThat(policy.email("own", false)).isTrue();
    assertThat(policy.email("other", false)).isFalse();
  }

  @Test
  void tickets() {
    login("USER");
    User other = new User();
    other.setId(2L);
    Ticket ticket = new Ticket();
    ticket.setUser(other);
    when(tickets.findByTicketCode("T")).thenReturn(Optional.of(ticket));
    assertThat(policy.ticket("T", true)).isFalse();
    assertThat(policy.ticket("T", false)).isFalse();
    other.setId(1L);
    assertThat(policy.ticket("T", true)).isTrue();
    assertThat(policy.ticket("T", false)).isTrue();
  }

  @Test
  void staffCannotCancelOrListOthers() {
    login("STAFF");
    assertThat(policy.ticket("T", true)).isTrue();
    assertThat(policy.ticket("T", false)).isFalse();
    assertThat(policy.email("other", true)).isFalse();
  }

  @Test
  void adminOperationalAccess() {
    login("ADMIN");
    assertThat(policy.ticket("T", false)).isTrue();
    assertThat(policy.email("other", true)).isTrue();
    assertThat(policy.email("other", false)).isFalse();
  }
}
