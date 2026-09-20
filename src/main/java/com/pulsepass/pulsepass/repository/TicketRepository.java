package com.pulsepass.pulsepass.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByEventId(Long eventId);

    List<Ticket> findByUserId(Long userId);

        @Query("""
        SELECT SUM(t.price)
        FROM Ticket t
        WHERE t.event.id = :eventId
        AND t.status = com.pulsepass.pulsepass.domain.TicketStatus.PAID
    """)
    BigDecimal calculateEventRevenue(Long eventId);

}
