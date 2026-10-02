package com.pulsepass.pulsepass.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;


import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserEmailIgnoreCase(String email);

    List<Ticket> findByUserEmailIgnoreCaseAndStatus(String email, TicketStatus status);

    List<Ticket> findByEventEventCodeAndStatus(String eventCode, TicketStatus status);

    List<Ticket> findByEventEventDateAfterOrderByEventEventDateAsc(LocalDateTime date);

    long countByEventEventCodeAndStatus(
            String eventCode,
            TicketStatus status
    );
    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(
        String email
    );
    

}
