package com.pulsepass.pulsepass.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long>{
    List<Event> findByStatus(EventStatus status);

    List<Event> findByVenueId(Long venueId);

}
