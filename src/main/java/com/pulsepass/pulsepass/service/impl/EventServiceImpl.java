package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.EventStatus;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.EventMapper;
import com.pulsepass.pulsepass.repository.ArtistRepository;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.VenueRepository;
import com.pulsepass.pulsepass.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(
            EventRepository eventRepository,
            VenueRepository venueRepository,
            ArtistRepository artistRepository,
            EventMapper eventMapper
    ) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.artistRepository = artistRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request) {

        if (eventRepository.existsByEventCode(request.eventCode())) {
            throw new DuplicateResourceException(
                    "Event code already exists: " + request.eventCode()
            );
        }

        Venue venue = venueRepository
                .findByCode(request.venueCode())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Venue not found with code: "
                                        + request.venueCode()
                        )
                );

        if (!venue.getActive()) {
            throw new BusinessRuleException(
                    "Cannot create an event in an inactive venue"
            );
        }

        if (request.eventDate() == null
                || !request.eventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Event date must be in the future"
            );
        }

        if (request.minimumAge() == null
                || request.minimumAge() < 0) {
            throw new BusinessRuleException(
                    "Minimum age must be greater than or equal to zero"
            );
        }

        Event event = new Event();

        event.setEventCode(request.eventCode());
        event.setName(request.name());
        event.setDescription(request.description());
        event.setCategory(request.category());
        event.setEventDate(request.eventDate());
        event.setMinimumAge(request.minimumAge());
        event.setVenue(venue);

        event.setStatus(EventStatus.DRAFT);

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    public EventResponse findByCode(String eventCode) {

        Event event = findEventByCode(eventCode);

        return eventMapper.toResponse(event);
    }

    @Override
    public List<EventSummaryResponse> findPublishedEvents() {

        List<Event> events =
                eventRepository.findByStatusOrderByEventDateAsc(
                        EventStatus.PUBLISHED
                );

        return eventMapper.toSummaryResponseList(events);
    }

    @Override
    @Transactional
    public EventResponse publish(String eventCode) {

        Event event = findEventByCode(eventCode);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only DRAFT events can be published"
            );
        }

        if (!event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException(
                    "Event date must be in the future"
            );
        }

        if (!event.getVenue().getActive()) {
            throw new BusinessRuleException(
                    "Cannot publish an event with an inactive venue"
            );
        }

        event.setStatus(EventStatus.PUBLISHED);

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    @Transactional
    public EventResponse addArtist(
            String eventCode,
            Long artistId
    ) {

        Event event = findEventByCode(eventCode);

        Artist artist = artistRepository
                .findById(artistId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Artist not found with id: " + artistId
                        )
                );

        if (event.getStatus() == EventStatus.CANCELLED
                || event.getStatus() == EventStatus.FINISHED) {

            throw new BusinessRuleException(
                    "Cannot add artists to CANCELLED or FINISHED events"
            );
        }

        if (event.getArtists().contains(artist)) {
            throw new BusinessRuleException(
                    "Artist is already associated with the event"
            );
        }

        event.getArtists().add(artist);

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    public List<EventSummaryResponse> findByArtist(
            String stageName
    ) {

        List<Event> events =
                eventRepository.findEventsByArtist(stageName);

        return eventMapper.toSummaryResponseList(events);
    }

    private Event findEventByCode(String eventCode) {

        return eventRepository
                .findByEventCode(eventCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found with code: " + eventCode
                        )
                );
    }
}