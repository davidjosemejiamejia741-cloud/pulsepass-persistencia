package com.pulsepass.pulsepass.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.mapper.TicketMapper;
import com.pulsepass.pulsepass.repository.*;
import com.pulsepass.pulsepass.service.*;
import com.pulsepass.pulsepass.service.impl.TicketServiceImpl;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(MethodSecurityTest.Config.class)
class MethodSecurityTest {
  @Configuration
  @EnableMethodSecurity
  @Import({TicketServiceImpl.class, AuthorizationService.class})
  static class Config {
    @Bean
    UserRepository users() {
      return mock(UserRepository.class);
    }

    @Bean
    TicketRepository tickets() {
      return mock(TicketRepository.class);
    }

    @Bean
    EventRepository events() {
      return mock(EventRepository.class);
    }

    @Bean
    TicketMapper mapper() {
      return mock(TicketMapper.class);
    }
  }

  @Autowired TicketService service;
  @Autowired UserRepository users;
  @Autowired TicketRepository tickets;
  @Autowired EventRepository events;

  @BeforeEach
  void before() {
    reset(users, tickets, events);
    login("USER");
  }

  @AfterEach
  void clear() {
    SecurityContextHolder.clearContext();
  }

  void login(String role) {
    var p = new SecurityPrincipal(1L, "a", "a@x.co", "hash", true, List.of(role));
    SecurityContextHolder.getContext()
        .setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(p, null, p.getAuthorities()));
  }

  Ticket ticket(long owner) {
    var u = new User();
    u.setId(owner);
    var e = new Event();
    e.setEventDate(LocalDateTime.now().plusDays(1));
    var t = new Ticket();
    t.setUser(u);
    t.setEvent(e);
    t.setStatus(TicketStatus.PAID);
    when(tickets.findByTicketCode("T")).thenReturn(Optional.of(t));
    return t;
  }

  @Test
  void foreignPurchaseNeverReachesBusiness() {
    var u = new User();
    u.setId(2L);
    when(users.findByEmailIgnoreCase("other@x.co")).thenReturn(Optional.of(u));
    assertThatThrownBy(
            () ->
                service.purchase(new PurchaseTicketRequest("other@x.co", "E", TicketType.GENERAL)))
        .isInstanceOf(AccessDeniedException.class);
    verifyNoInteractions(events, tickets);
  }

  @Test
  void foreignReadAndCancelDeniedBeforeMutation() {
    var t = ticket(2L);
    assertThatThrownBy(() -> service.findByCode("T")).isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> service.cancel("T")).isInstanceOf(AccessDeniedException.class);
    assertThat(t.getStatus()).isEqualTo(TicketStatus.PAID);
    verify(tickets, never()).save(any());
  }

  @Test
  void ownerCancelPreservesBusiness() {
    var t = ticket(1L);
    when(tickets.save(t)).thenReturn(t);
    service.cancel("T");
    assertThat(t.getStatus()).isEqualTo(TicketStatus.CANCELLED);
    verify(tickets).save(t);
  }

  @Test
  void userCannotUseEvenOwnTicket() {
    assertThatThrownBy(() -> service.markAsUsed("T")).isInstanceOf(AccessDeniedException.class);
    verifyNoInteractions(tickets);
  }

  @Test
  void staffUseStillEnforcesTransition() {
    login("STAFF");
    var t = ticket(2L);
    t.setStatus(TicketStatus.USED);
    assertThatThrownBy(() -> service.markAsUsed("T"))
        .isInstanceOf(com.pulsepass.pulsepass.exception.BusinessRuleException.class);
    verify(tickets, never()).save(any());
  }
}
