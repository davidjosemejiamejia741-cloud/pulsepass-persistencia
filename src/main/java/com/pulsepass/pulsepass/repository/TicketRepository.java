package com.pulsepass.pulsepass.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUserEmailIgnoreCase(String email);

    List<Ticket> findByUserEmailIgnoreCaseAndStatus(String email, TicketStatus status);

    List<Ticket> findByEventEventCodeAndStatus(String eventCode, TicketStatus status);

    List<Ticket> findByEventEventDateAfterOrderByEventEventDateAsc(LocalDateTime date);

        @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.event.eventCode = :eventCode
        AND t.status = com.pulsepass.pulsepass.domain.TicketStatus.PAID
    """)
    long countPaidTicketsByEventCode(String eventCode);

}
