package com.pulsepass.pulsepass.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.EventMapper;
import com.pulsepass.pulsepass.repository.*;
import com.pulsepass.pulsepass.service.impl.EventServiceImpl;
import java.util.*;
import org.junit.jupiter.api.*;

class EventVisibilityTest {
  EventRepository events = mock(EventRepository.class);
  EventMapper mapper = mock(EventMapper.class);
  EventServiceImpl service =
      new EventServiceImpl(
          events, mock(VenueRepository.class), mock(ArtistRepository.class), mapper);

  @AfterEach
  void clear() {
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }

  @Test
  void anonymousDraftHidden() {
    Event draft = new Event();
    draft.setStatus(EventStatus.DRAFT);
    when(events.findByEventCode("D")).thenReturn(Optional.of(draft));
    assertThatThrownBy(() -> service.findByCode("D")).isInstanceOf(ResourceNotFoundException.class);
    verifyNoInteractions(mapper);
  }

  @Test
  void artistListFiltersDraft() {
    Event draft = new Event();
    draft.setStatus(EventStatus.DRAFT);
    Event published = new Event();
    published.setStatus(EventStatus.PUBLISHED);
    when(events.findEventsByArtist("A")).thenReturn(List.of(draft, published));
    service.findByArtist("A");
    verify(mapper).toSummaryResponseList(List.of(published));
  }

  @Test
  void staffSeesDraft() {
    Event draft = new Event();
    draft.setStatus(EventStatus.DRAFT);
    when(events.findByEventCode("D")).thenReturn(Optional.of(draft));
    var p = new SecurityPrincipal(1L, "staff", "s@x.co", "hash", true, List.of("STAFF"));
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                .authenticated(p, null, p.getAuthorities()));
    service.findByCode("D");
    verify(mapper).toResponse(draft);
  }
}
