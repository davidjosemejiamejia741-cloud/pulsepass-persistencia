package com.pulsepass.pulsepass.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long>{
    Optional<Event> findByEventCode(String eventCode);

    boolean existsByEventCode(String eventCode);

    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    List<Event> findByVenueCode(String code);

        @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN e.artists a
        WHERE a.stageName = :stageName
    """)
    List<Event> findEventsByArtist(
            @Param("stageName") String stageName
    );

    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN e.artists a
        WHERE e.venue.city = :city
          AND a.stageName = :stageName
        ORDER BY e.eventDate ASC
    """)
    List<Event> findEventsByCityAndArtist(@Param("city") String city,
                                          @Param("stageName") String stageName);

    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN e.artists a
        WHERE e.status = com.pulsepass.pulsepass.domain.EventStatus.PUBLISHED
          AND e.eventDate > :date
          AND e.venue.city = :city
          AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistText, '%'))
        ORDER BY e.eventDate ASC
    """)
    List<Event> findRecommendedEvents(@Param("date") LocalDateTime date,
                                      @Param("city") String city,
                                      @Param("artistText") String artistText);

}
