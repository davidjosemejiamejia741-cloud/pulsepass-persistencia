package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.EventCategory;
import com.pulsepass.pulsepass.domain.EventStatus;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.EventMapper;
import com.pulsepass.pulsepass.repository.ArtistRepository;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.VenueRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    // TEST-EVENT-001
    @Test
    void shouldReturnEventWhenEventExists() {

        // ARRANGE
        Event event = createEvent(EventStatus.PUBLISHED);

        EventResponse response =
                createEventResponse(EventStatus.PUBLISHED);

        when(eventRepository.findByEventCode("CMF-2026"))
                .thenReturn(Optional.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        // ACT
        EventResponse result =
                eventService.findByCode("CMF-2026");

        // ASSERT
        assertThat(result)
                .isEqualTo(response);

        assertThat(result.eventCode())
                .isEqualTo("CMF-2026");

        verify(eventRepository)
                .findByEventCode(eq("CMF-2026"));

        verify(eventMapper)
                .toResponse(event);
    }

    // TEST-EVENT-002
    @Test
    void shouldThrowResourceNotFoundExceptionWhenEventDoesNotExist() {

        // ARRANGE
        when(eventRepository.findByEventCode("UNKNOWN"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(
                () -> eventService.findByCode("UNKNOWN")
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );

        verify(eventRepository)
                .findByEventCode("UNKNOWN");

        verify(eventMapper, never())
                .toResponse(any(Event.class));
    }

    // TEST-EVENT-003
    @Test
    void shouldCreateValidEventAndSaveIt() {

        // ARRANGE
        Venue venue = createVenue(true);

        CreateEventRequest request =
                validCreateRequest();

        EventResponse response =
                createEventResponse(EventStatus.DRAFT);

        when(eventRepository.existsByEventCode("CMF-2026"))
                .thenReturn(false);

        when(venueRepository.findByCode("VEN-SMR-01"))
                .thenReturn(Optional.of(venue));

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        when(eventMapper.toResponse(any(Event.class)))
                .thenReturn(response);

        // ACT
        EventResponse result =
                eventService.create(request);

        // ASSERT
        assertThat(result)
                .isEqualTo(response);

        ArgumentCaptor<Event> captor =
                ArgumentCaptor.forClass(Event.class);

        verify(eventRepository)
                .save(captor.capture());

        Event savedEvent =
                captor.getValue();

        assertThat(savedEvent.getEventCode())
                .isEqualTo("CMF-2026");

        assertThat(savedEvent.getStatus())
                .isEqualTo(EventStatus.DRAFT);

        assertThat(savedEvent.getVenue())
                .isEqualTo(venue);

        assertThat(savedEvent.getMinimumAge())
                .isEqualTo(18);
    }

    // TEST-EVENT-004
    @Test
    void shouldNotSaveWhenVenueDoesNotExist() {

        // ARRANGE
        CreateEventRequest request =
                validCreateRequest();

        when(eventRepository.existsByEventCode("CMF-2026"))
                .thenReturn(false);

        when(venueRepository.findByCode("VEN-SMR-01"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(
                () -> eventService.create(request)
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    // TEST-EVENT-005
    @Test
    void shouldThrowBusinessRuleExceptionWhenVenueIsInactive() {

        // ARRANGE
        Venue venue =
                createVenue(false);

        when(eventRepository.existsByEventCode("CMF-2026"))
                .thenReturn(false);

        when(venueRepository.findByCode("VEN-SMR-01"))
                .thenReturn(Optional.of(venue));

        // ACT + ASSERT
        assertThatThrownBy(
                () -> eventService.create(
                        validCreateRequest()
                )
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    // TEST-EVENT-006
    @Test
    void shouldThrowBusinessRuleExceptionWhenEventDateIsPast() {

        // ARRANGE
        Venue venue =
                createVenue(true);

        CreateEventRequest request =
                new CreateEventRequest(
                        "CMF-2026",
                        "Caribbean Music Fest 2026",
                        "Music festival",
                        EventCategory.MUSIC,
                        LocalDateTime.now().minusDays(1),
                        18,
                        "VEN-SMR-01"
                );

        when(eventRepository.existsByEventCode("CMF-2026"))
                .thenReturn(false);

        when(venueRepository.findByCode("VEN-SMR-01"))
                .thenReturn(Optional.of(venue));

        // ACT + ASSERT
        assertThatThrownBy(
                () -> eventService.create(request)
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    // TEST-EVENT-007
    @Test
    void shouldPublishValidDraftEvent() {

        // ARRANGE
        Event event =
                createEvent(EventStatus.DRAFT);

        EventResponse response =
                createEventResponse(
                        EventStatus.PUBLISHED
                );

        when(eventRepository.findByEventCode("CMF-2026"))
                .thenReturn(Optional.of(event));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        // ACT
        EventResponse result =
                eventService.publish("CMF-2026");

        // ASSERT
        assertThat(event.getStatus())
                .isEqualTo(EventStatus.PUBLISHED);

        assertThat(result.status())
                .isEqualTo(EventStatus.PUBLISHED);

        verify(eventRepository)
                .save(event);
    }

    // TEST-EVENT-008
    @Test
    void shouldNotPublishCancelledEvent() {

        // ARRANGE
        Event event =
                createEvent(EventStatus.CANCELLED);

        when(eventRepository.findByEventCode("CMF-2026"))
                .thenReturn(Optional.of(event));

        // ACT + ASSERT
        assertThatThrownBy(
                () -> eventService.publish("CMF-2026")
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    // Matriz de trazabilidad FR-SVC-007
    @Test
    void shouldAddArtistToEvent() {

        // ARRANGE
        Event event =
                createEvent(EventStatus.DRAFT);

        Artist artist = new Artist();
        artist.setId(1L);
        artist.setStageName("Solar Beat");
        artist.setActive(true);

        EventResponse response =
                createEventResponse(EventStatus.DRAFT);

        when(eventRepository.findByEventCode("CMF-2026"))
                .thenReturn(Optional.of(event));

        when(artistRepository.findById(1L))
                .thenReturn(Optional.of(artist));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        // ACT
        EventResponse result =
                eventService.addArtist(
                        "CMF-2026",
                        1L
                );

        // ASSERT
        assertThat(result)
                .isEqualTo(response);

        assertThat(event.getArtists())
                .contains(artist);

        verify(artistRepository)
                .findById(eq(1L));

        verify(eventRepository)
                .save(event);
    }

    private CreateEventRequest validCreateRequest() {

        return new CreateEventRequest(
                "CMF-2026",
                "Caribbean Music Fest 2026",
                "Music festival",
                EventCategory.MUSIC,
                LocalDateTime.now().plusMonths(3),
                18,
                "VEN-SMR-01"
        );
    }

    private Venue createVenue(
            boolean active
    ) {

        Venue venue = new Venue();

        venue.setId(1L);
        venue.setCode("VEN-SMR-01");
        venue.setName(
                "Marina Convention Center"
        );
        venue.setCity("Santa Marta");
        venue.setAddress("Marina");
        venue.setCapacity(3);
        venue.setActive(active);

        return venue;
    }

    private Event createEvent(
            EventStatus status
    ) {

        Event event = new Event();

        event.setId(1L);
        event.setEventCode("CMF-2026");

        event.setName(
                "Caribbean Music Fest 2026"
        );

        event.setDescription(
                "Music festival"
        );

        event.setCategory(
                EventCategory.MUSIC
        );

        event.setStatus(status);

        event.setEventDate(
                LocalDateTime.now()
                        .plusMonths(3)
        );

        event.setMinimumAge(18);

        event.setVenue(
                createVenue(true)
        );

        event.setArtists(
                new HashSet<>()
        );

        return event;
    }

    private EventResponse createEventResponse(
            EventStatus status
    ) {

        return new EventResponse(
                1L,
                "CMF-2026",
                "Caribbean Music Fest 2026",
                "Music festival",
                EventCategory.MUSIC,
                status,
                LocalDateTime.now()
                        .plusMonths(3),
                18,
                "VEN-SMR-01",
                "Marina Convention Center",
                Set.of()
        );
    }
}