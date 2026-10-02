package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.EventStatus;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.TicketStatus;
import com.pulsepass.pulsepass.domain.TicketType;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.TicketMapper;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.TicketRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.service.TicketService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private static final BigDecimal GENERAL_PRICE =
            new BigDecimal("100000.00");

    private static final BigDecimal STUDENT_PRICE =
            new BigDecimal("80000.00");

    private static final BigDecimal VIP_PRICE =
            new BigDecimal("150000.00");

    private static final BigDecimal BACKSTAGE_PRICE =
            new BigDecimal("250000.00");

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            TicketMapper ticketMapper
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketMapper = ticketMapper;
    }

    @Override
    @Transactional
    public TicketResponse purchase(
            PurchaseTicketRequest request
    ) {

        User user = userRepository
                .findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: "
                                        + request.userEmail()
                        )
                );

        validateUser(user);

        Event event = eventRepository
                .findByEventCode(request.eventCode())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found: "
                                        + request.eventCode()
                        )
                );

        validateEvent(event);

        validateMinimumAge(
                user,
                event
        );

        long paidTickets =
                ticketRepository
                        .countByEventEventCodeAndStatus(
                                event.getEventCode(),
                                TicketStatus.PAID
                        );

        int capacity =
                event.getVenue().getCapacity();

        if (paidTickets >= capacity) {
            throw new BusinessRuleException(
                    "Event has no available capacity."
            );
        }

        BigDecimal price =
                calculatePrice(request.type());

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException(
                    "Ticket price cannot be negative."
            );
        }

        Ticket ticket = new Ticket();

        ticket.setTicketCode(
                generateTicketCode()
        );

        ticket.setType(
                request.type()
        );

        ticket.setPrice(
                price
        );

        ticket.setStatus(
                TicketStatus.PAID
        );

        ticket.setPurchaseDate(
                LocalDateTime.now()
        );

        ticket.setUser(
                user
        );

        ticket.setEvent(
                event
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        if (paidTickets + 1 == capacity) {

            event.setStatus(
                    EventStatus.SOLD_OUT
            );

            eventRepository.save(event);
        }

        return ticketMapper.toResponse(
                savedTicket
        );
    }

    @Override
    public TicketResponse findByCode(
            String ticketCode
    ) {

        Ticket ticket =
                findTicketByCode(ticketCode);

        return ticketMapper.toResponse(
                ticket
        );
    }

    @Override
    public List<TicketResponse> findByUserEmail(
            String email
    ) {

        List<Ticket> tickets =
                ticketRepository
                        .findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(
                                email
                        );

        return ticketMapper.toResponseList(
                tickets
        );
    }

    @Override
    public List<TicketResponse> findPaidTicketsByEvent(
            String eventCode
    ) {

        List<Ticket> tickets =
                ticketRepository
                        .findByEventEventCodeAndStatus(
                                eventCode,
                                TicketStatus.PAID
                        );

        return ticketMapper.toResponseList(
                tickets
        );
    }

    @Override
    @Transactional
    public TicketResponse cancel(
            String ticketCode
    ) {

        Ticket ticket =
                findTicketByCode(ticketCode);

        if (ticket.getStatus()
                != TicketStatus.PAID) {

            throw new BusinessRuleException(
                    "Only PAID tickets can be cancelled."
            );
        }

        if (LocalDateTime.now()
                .isAfter(
                        ticket.getEvent()
                                .getEventDate()
                )) {

            throw new BusinessRuleException(
                    "Ticket cannot be cancelled after the event date."
            );
        }

        ticket.setStatus(
                TicketStatus.CANCELLED
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        return ticketMapper.toResponse(
                savedTicket
        );
    }

    @Override
    @Transactional
    public TicketResponse markAsUsed(
            String ticketCode
    ) {

        Ticket ticket =
                findTicketByCode(ticketCode);

        if (ticket.getStatus()
                != TicketStatus.PAID) {

            throw new BusinessRuleException(
                    "Only PAID tickets can be marked as used."
            );
        }

        ticket.setStatus(
                TicketStatus.USED
        );

        Ticket savedTicket =
                ticketRepository.save(ticket);

        return ticketMapper.toResponse(
                savedTicket
        );
    }

    private Ticket findTicketByCode(
            String ticketCode
    ) {

        return ticketRepository
                .findByTicketCode(ticketCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found: "
                                        + ticketCode
                        )
                );
    }

    private void validateUser(
            User user
    ) {

        if (!Boolean.TRUE.equals(
                user.getActive()
        )) {

            throw new BusinessRuleException(
                    "Inactive users cannot purchase tickets."
            );
        }
    }

    private void validateEvent(
            Event event
    ) {

        if (event.getStatus()
                != EventStatus.PUBLISHED) {

            throw new BusinessRuleException(
                    "Tickets can only be purchased for PUBLISHED events."
            );
        }

        if (!event.getEventDate()
                .isAfter(LocalDateTime.now())) {

            throw new BusinessRuleException(
                    "Cannot purchase tickets for an event that has already occurred."
            );
        }
    }

    private void validateMinimumAge(
            User user,
            Event event
    ) {

        if (event.getMinimumAge() <= 0) {
            return;
        }

        if (user.getProfile() == null
                || user.getProfile()
                        .getBirthDate() == null) {

            throw new BusinessRuleException(
                    "Birth date is required to validate minimum age."
            );
        }

        LocalDate birthDate =
                user.getProfile()
                        .getBirthDate();

        LocalDate eventDate =
                event.getEventDate()
                        .toLocalDate();

        int age = Period
                .between(
                        birthDate,
                        eventDate
                )
                .getYears();

        if (age < event.getMinimumAge()) {

            throw new BusinessRuleException(
                    "User does not meet minimum age."
            );
        }
    }

    private BigDecimal calculatePrice(
            TicketType type
    ) {

        return switch (type) {

            case GENERAL ->
                    GENERAL_PRICE;

            case STUDENT ->
                    STUDENT_PRICE;

            case VIP ->
                    VIP_PRICE;

            case BACKSTAGE ->
                    BACKSTAGE_PRICE;
        };
    }

    private String generateTicketCode() {

        return "TKT-"
                + UUID.randomUUID();
    }
}