package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.EventCategory;
import com.pulsepass.pulsepass.domain.EventStatus;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.TicketStatus;
import com.pulsepass.pulsepass.domain.TicketType;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserProfile;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.TicketMapper;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.TicketRepository;
import com.pulsepass.pulsepass.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    // TEST-TICKET-001
    @Test
    void shouldPurchaseValidTicket() {

        // ARRANGE
        User user = createActiveAdultUser();
        Event event = createEvent(EventStatus.PUBLISHED, 3);

        PurchaseTicketRequest request =
                validPurchaseRequest();

        TicketResponse response =
                createTicketResponse(
                        TicketStatus.PAID
                );

        when(
                userRepository.findByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                eventRepository.findByEventCode(
                        "CMF-2026"
                )
        ).thenReturn(Optional.of(event));

        when(
                ticketRepository
                        .countByEventEventCodeAndStatus(
                                "CMF-2026",
                                TicketStatus.PAID
                        )
        ).thenReturn(0L);

        when(
                ticketRepository.save(
                        any(Ticket.class)
                )
        ).thenAnswer(invocation -> {

            Ticket ticket =
                    invocation.getArgument(0);

            ticket.setId(1L);

            return ticket;
        });

        when(
                ticketMapper.toResponse(
                        any(Ticket.class)
                )
        ).thenReturn(response);

        // ACT
        TicketResponse result =
                ticketService.purchase(request);

        // ASSERT
        assertThat(result.status())
                .isEqualTo(TicketStatus.PAID);

        ArgumentCaptor<Ticket> captor =
                ArgumentCaptor.forClass(
                        Ticket.class
                );

        verify(ticketRepository)
                .save(captor.capture());

        Ticket savedTicket =
                captor.getValue();

        assertThat(savedTicket.getStatus())
                .isEqualTo(TicketStatus.PAID);

        assertThat(savedTicket.getUser())
                .isEqualTo(user);

        assertThat(savedTicket.getEvent())
                .isEqualTo(event);

        assertThat(savedTicket.getPrice())
                .isEqualByComparingTo(
                        new BigDecimal("100000.00")
                );

        verify(
                eventRepository,
                never()
        ).save(any(Event.class));
    }

    // TEST-TICKET-002
    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {

        // ARRANGE
        when(
                userRepository.findByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(
                () -> ticketService.purchase(
                        validPurchaseRequest()
                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                );

        verify(
                eventRepository,
                never()
        ).findByEventCode(any());

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));
    }

    // TEST-TICKET-003
    @Test
    void shouldThrowBusinessRuleExceptionWhenUserIsInactive() {

        // ARRANGE
        User user =
                createActiveAdultUser();

        user.setActive(false);

        when(
                userRepository.findByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(Optional.of(user));

        // ACT + ASSERT
        assertThatThrownBy(
                () -> ticketService.purchase(
                        validPurchaseRequest()
                )
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(
                eventRepository,
                never()
        ).findByEventCode(any());

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));
    }

    // TEST-TICKET-004
    @Test
    void shouldThrowBusinessRuleExceptionWhenEventIsDraft() {

        // ARRANGE
        User user =
                createActiveAdultUser();

        Event event =
                createEvent(
                        EventStatus.DRAFT,
                        3
                );

        when(
                userRepository.findByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                eventRepository.findByEventCode(
                        "CMF-2026"
                )
        ).thenReturn(Optional.of(event));

        // ACT + ASSERT
        assertThatThrownBy(
                () -> ticketService.purchase(
                        validPurchaseRequest()
                )
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));
    }

    // TEST-TICKET-005
    @Test
    void shouldThrowBusinessRuleExceptionWhenEventIsCancelled() {

        // ARRANGE
        User user =
                createActiveAdultUser();

        Event event =
                createEvent(
                        EventStatus.CANCELLED,
                        3
                );

        when(
                userRepository.findByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                eventRepository.findByEventCode(
                        "CMF-2026"
                )
        ).thenReturn(Optional.of(event));

        // ACT + ASSERT
        assertThatThrownBy(
                () -> ticketService.purchase(
                        validPurchaseRequest()
                )
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));
    }

    // TEST-TICKET-006
    @Test
    void shouldThrowBusinessRuleExceptionWhenUserIsUnderAge() {

        // ARRANGE
        Event event =
                createEvent(
                        EventStatus.PUBLISHED,
                        3
                );

        User user =
                createUserWithAgeAtEvent(
                        event,
                        17
                );

        when(
                userRepository.findByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                eventRepository.findByEventCode(
                        "CMF-2026"
                )
        ).thenReturn(Optional.of(event));

        // ACT + ASSERT
        assertThatThrownBy(
                () -> ticketService.purchase(
                        validPurchaseRequest()
                )
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(
                ticketRepository,
                never()
        ).countByEventEventCodeAndStatus(
                any(),
                any()
        );

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));
    }

    // TEST-TICKET-007
    @Test
    void shouldThrowBusinessRuleExceptionWhenEventHasNoCapacity() {

        // ARRANGE
        User user =
                createActiveAdultUser();

        Event event =
                createEvent(
                        EventStatus.PUBLISHED,
                        3
                );

        when(
                userRepository.findByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                eventRepository.findByEventCode(
                        "CMF-2026"
                )
        ).thenReturn(Optional.of(event));

        when(
                ticketRepository
                        .countByEventEventCodeAndStatus(
                                eq("CMF-2026"),
                                eq(TicketStatus.PAID)
                        )
        ).thenReturn(3L);

        // ACT + ASSERT
        assertThatThrownBy(
                () -> ticketService.purchase(
                        validPurchaseRequest()
                )
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));
    }

    // TEST-TICKET-008
    @Test
    void shouldMarkEventAsSoldOutWhenPurchasingLastTicket() {

        // ARRANGE
        User user =
                createActiveAdultUser();

        Event event =
                createEvent(
                        EventStatus.PUBLISHED,
                        3
                );

        when(
                userRepository.findByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(Optional.of(user));

        when(
                eventRepository.findByEventCode(
                        "CMF-2026"
                )
        ).thenReturn(Optional.of(event));

        when(
                ticketRepository
                        .countByEventEventCodeAndStatus(
                                "CMF-2026",
                                TicketStatus.PAID
                        )
        ).thenReturn(2L);

        when(
                ticketRepository.save(
                        any(Ticket.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                eventRepository.save(event)
        ).thenReturn(event);

        when(
                ticketMapper.toResponse(
                        any(Ticket.class)
                )
        ).thenReturn(
                createTicketResponse(
                        TicketStatus.PAID
                )
        );

        // ACT
        TicketResponse result =
                ticketService.purchase(
                        validPurchaseRequest()
                );

        // ASSERT
        assertThat(result.status())
                .isEqualTo(TicketStatus.PAID);

        assertThat(event.getStatus())
                .isEqualTo(
                        EventStatus.SOLD_OUT
                );

        verify(ticketRepository)
                .save(any(Ticket.class));

        verify(eventRepository)
                .save(event);
    }

    // TEST-TICKET-009
    @Test
    void shouldCancelPaidTicket() {

        // ARRANGE
        Ticket ticket =
                createTicket(
                        TicketStatus.PAID
                );

        when(
                ticketRepository.findByTicketCode(
                        "TKT-001"
                )
        ).thenReturn(Optional.of(ticket));

        when(
                ticketRepository.save(ticket)
        ).thenReturn(ticket);

        when(
                ticketMapper.toResponse(ticket)
        ).thenReturn(
                createTicketResponse(
                        TicketStatus.CANCELLED
                )
        );

        // ACT
        TicketResponse result =
                ticketService.cancel(
                        "TKT-001"
                );

        // ASSERT
        assertThat(ticket.getStatus())
                .isEqualTo(
                        TicketStatus.CANCELLED
                );

        assertThat(result.status())
                .isEqualTo(
                        TicketStatus.CANCELLED
                );

        verify(ticketRepository)
                .save(ticket);
    }

    // TEST-TICKET-010
    @Test
    void shouldThrowBusinessRuleExceptionWhenCancellingUsedTicket() {

        // ARRANGE
        Ticket ticket =
                createTicket(
                        TicketStatus.USED
                );

        when(
                ticketRepository.findByTicketCode(
                        "TKT-001"
                )
        ).thenReturn(Optional.of(ticket));

        // ACT + ASSERT
        assertThatThrownBy(
                () -> ticketService.cancel(
                        "TKT-001"
                )
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));
    }

    // TEST-TICKET-011
    @Test
    void shouldMarkPaidTicketAsUsed() {

        // ARRANGE
        Ticket ticket =
                createTicket(
                        TicketStatus.PAID
                );

        when(
                ticketRepository.findByTicketCode(
                        "TKT-001"
                )
        ).thenReturn(Optional.of(ticket));

        when(
                ticketRepository.save(ticket)
        ).thenReturn(ticket);

        when(
                ticketMapper.toResponse(ticket)
        ).thenReturn(
                createTicketResponse(
                        TicketStatus.USED
                )
        );

        // ACT
        TicketResponse result =
                ticketService.markAsUsed(
                        "TKT-001"
                );

        // ASSERT
        assertThat(ticket.getStatus())
                .isEqualTo(
                        TicketStatus.USED
                );

        assertThat(result.status())
                .isEqualTo(
                        TicketStatus.USED
                );

        verify(ticketRepository)
                .save(ticket);
    }

    // TEST-TICKET-012
    @Test
    void shouldThrowBusinessRuleExceptionWhenUsingCancelledTicket() {

        // ARRANGE
        Ticket ticket =
                createTicket(
                        TicketStatus.CANCELLED
                );

        when(
                ticketRepository.findByTicketCode(
                        "TKT-001"
                )
        ).thenReturn(Optional.of(ticket));

        // ACT + ASSERT
        assertThatThrownBy(
                () -> ticketService.markAsUsed(
                        "TKT-001"
                )
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(
                ticketRepository,
                never()
        ).save(any(Ticket.class));
    }

    private PurchaseTicketRequest validPurchaseRequest() {

        return new PurchaseTicketRequest(
                "andrea@email.com",
                "CMF-2026",
                TicketType.GENERAL
        );
    }

    private Venue createVenue(
            int capacity
    ) {

        Venue venue = new Venue();

        venue.setId(1L);
        venue.setCode("VEN-SMR-01");
        venue.setName(
                "Marina Convention Center"
        );
        venue.setCity("Santa Marta");
        venue.setCapacity(capacity);
        venue.setActive(true);

        return venue;
    }

    private Event createEvent(
            EventStatus status,
            int capacity
    ) {

        Event event = new Event();

        event.setId(1L);
        event.setEventCode("CMF-2026");

        event.setName(
                "Caribbean Music Fest 2026"
        );

        event.setCategory(
                EventCategory.MUSIC
        );

        event.setStatus(status);

        event.setEventDate(
                LocalDateTime.now()
                        .plusMonths(6)
        );

        event.setMinimumAge(18);

        event.setVenue(
                createVenue(capacity)
        );

        return event;
    }

    private User createActiveAdultUser() {

        User user = new User();

        user.setId(1L);
        user.setUsername("andrea");
        user.setEmail(
                "andrea@email.com"
        );
        user.setActive(true);

        UserProfile profile =
                new UserProfile();

        profile.setBirthDate(
                LocalDate.now()
                        .minusYears(25)
        );

        profile.setUser(user);

        user.setProfile(profile);

        return user;
    }

    private User createUserWithAgeAtEvent(
            Event event,
            int age
    ) {

        User user =
                createActiveAdultUser();

        LocalDate eventDate =
                event.getEventDate()
                        .toLocalDate();

        user.getProfile()
                .setBirthDate(
                        eventDate.minusYears(age)
                );

        return user;
    }

    private Ticket createTicket(
            TicketStatus status
    ) {

        Ticket ticket = new Ticket();

        ticket.setId(1L);
        ticket.setTicketCode(
                "TKT-001"
        );

        ticket.setType(
                TicketType.GENERAL
        );

        ticket.setPrice(
                new BigDecimal(
                        "100000.00"
                )
        );

        ticket.setStatus(status);

        ticket.setPurchaseDate(
                LocalDateTime.now()
        );

        ticket.setUser(
                createActiveAdultUser()
        );

        ticket.setEvent(
                createEvent(
                        EventStatus.PUBLISHED,
                        3
                )
        );

        return ticket;
    }

    private TicketResponse createTicketResponse(
            TicketStatus status
    ) {

        return new TicketResponse(
                1L,
                "TKT-001",
                TicketType.GENERAL,
                new BigDecimal(
                        "100000.00"
                ),
                status,
                LocalDateTime.now(),
                "andrea@email.com",
                "CMF-2026",
                "Caribbean Music Fest 2026"
        );
    }
}