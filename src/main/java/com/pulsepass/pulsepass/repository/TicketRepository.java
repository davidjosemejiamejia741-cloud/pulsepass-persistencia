package com.pulsepass.pulsepass.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByEventId(Long eventId);

    List<Ticket> findByUserId(Long userId);

}
