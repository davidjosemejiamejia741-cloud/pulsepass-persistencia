package com.pulsepass.pulsepass.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType type;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;


    @ManyToOne
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    private User user;


    @ManyToOne
    @JoinColumn(
        name = "event_id",
        nullable = false
    )
    private Event event;

}