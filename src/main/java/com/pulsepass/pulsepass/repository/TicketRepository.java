package com.pulsepass.pulsepass.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsepass.pulsepass.domain.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
