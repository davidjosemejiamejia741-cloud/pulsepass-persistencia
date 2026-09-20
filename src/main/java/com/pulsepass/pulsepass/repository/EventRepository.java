package com.pulsepass.pulsepass.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long>{
    List<Event> findByStatus(EventStatus status);

    List<Event> findByVenueId(Long venueId);

        @Query("""
        SELECT e
        FROM Event e
        JOIN e.artists a
        WHERE a.stageName = :stageName
    """)
    List<Event> findEventsByArtist(
            @Param("stageName") String stageName
    );

}
