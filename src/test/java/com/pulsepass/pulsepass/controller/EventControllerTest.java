package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.domain.EventCategory;
import com.pulsepass.pulsepass.domain.EventStatus;
import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.context.annotation.Import(com.pulsepass.pulsepass.security.MvcSecuritySupport.class)
@org.springframework.security.test.context.support.WithMockUser(roles={"USER","STAFF","ADMIN"})
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EventService service;

    private static final LocalDateTime DATE = LocalDateTime.of(2030, 10, 4, 20, 0);

    private static final ArtistResponse ARTIST = new ArtistResponse(
            1L,
            "Solar Beat",
            "Colombia",
            "Electronic",
            true
    );

    private static final EventSummaryResponse SUMMARY =
            new EventSummaryResponse(
                    1L,
                    "CMF-2026",
                    "Caribbean Music Fest 2026",
                    EventCategory.MUSIC,
                    EventStatus.PUBLISHED,
                    DATE,
                    "Marina Convention Center"
            );

    private static final String EVENT_JSON = """
            {
              "eventCode": "CMF-2026",
              "name": "Caribbean Music Fest 2026",
              "description": "Music festival",
              "category": "MUSIC",
              "eventDate": "2030-10-04T20:00:00",
              "minimumAge": 18,
              "venueCode": "VEN-SMR-01"
            }
            """;

    private static EventResponse event(EventStatus status) {
        return new EventResponse(
                1L,
                "CMF-2026",
                "Caribbean Music Fest 2026",
                "Music festival",
                EventCategory.MUSIC,
                status,
                DATE,
                18,
                "VEN-SMR-01",
                "Marina Convention Center",
                Set.of(ARTIST)
        );
    }

    private static CreateEventRequest validRequest() {
        return new CreateEventRequest(
                "CMF-2026",
                "Caribbean Music Fest 2026",
                "Music festival",
                EventCategory.MUSIC,
                DATE,
                18,
                "VEN-SMR-01"
        );
    }

    @Test
    void createEvent() throws Exception {
        CreateEventRequest request = validRequest();

        when(service.create(request)).thenReturn(event(EventStatus.DRAFT));

        mvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(EVENT_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(service).create(request);
    }

    @Test
    void requiredFieldsRejected() throws Exception {
        mvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.eventCode").isNotEmpty())
                .andExpect(jsonPath("$.details.name").isNotEmpty())
                .andExpect(jsonPath("$.details.category").isNotEmpty())
                .andExpect(jsonPath("$.details.eventDate").isNotEmpty())
                .andExpect(jsonPath("$.details.minimumAge").isNotEmpty())
                .andExpect(jsonPath("$.details.venueCode").isNotEmpty());

        verify(service, never()).create(any());
        verifyNoInteractions(service);
    }

    @Test
    void findEvent() throws Exception {
        when(service.findByCode("CMF-2026")).thenReturn(event(EventStatus.DRAFT));

        mvc.perform(get("/api/events/CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.venueCode").value("VEN-SMR-01"));

        verify(service).findByCode("CMF-2026");
    }

    @Test
    void missingEvent() throws Exception {
        when(service.findByCode("missing"))
                .thenThrow(new ResourceNotFoundException(
                        "Event not found: missing"
                ));

        mvc.perform(get("/api/events/missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Event not found: missing"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findByCode("missing");
    }

    @Test
    void publishedEvents() throws Exception {
        when(service.findPublishedEvents()).thenReturn(List.of(SUMMARY));

        mvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));

        verify(service).findPublishedEvents();
    }

    @Test
    void publishEvent() throws Exception {
        when(service.publish("CMF-2026")).thenReturn(event(EventStatus.PUBLISHED));

        mvc.perform(patch("/api/events/CMF-2026/publish"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(service).publish("CMF-2026");
    }

    @Test
    void publishConflict() throws Exception {
        when(service.publish("CMF-2026"))
                .thenThrow(new BusinessRuleException("Event cannot be published"));

        mvc.perform(patch("/api/events/CMF-2026/publish"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Event cannot be published"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).publish("CMF-2026");
    }

    @Test
    void addArtist() throws Exception {
        when(service.addArtist("CMF-2026", 1L))
                .thenReturn(event(EventStatus.DRAFT));

        mvc.perform(post("/api/events/CMF-2026/artists/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.artists[0].id").value(1))
                .andExpect(jsonPath("$.artists[0].stageName").value(
                        "Solar Beat"
                ));

        verify(service).addArtist("CMF-2026", 1L);
    }

    @Test
    void duplicateArtist() throws Exception {
        when(service.addArtist("CMF-2026", 1L))
                .thenThrow(new DuplicateResourceException(
                        "Artist already associated"
                ));

        mvc.perform(post("/api/events/CMF-2026/artists/1"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "Artist already associated"
                ));

        verify(service).addArtist("CMF-2026", 1L);
    }

    @Test
    void eventsByArtist() throws Exception {
        when(service.findByArtist("Solar Beat"))
                .thenReturn(List.of(SUMMARY));

        mvc.perform(get("/api/events/by-artist")
                .param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(service).findByArtist("Solar Beat");
    }

    @Test
    void duplicateEvent() throws Exception {
        CreateEventRequest request = validRequest();

        when(service.create(request))
                .thenThrow(new DuplicateResourceException(
                        "Event code already exists"
                ));

        mvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(EVENT_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Event code already exists"
                ));

        verify(service).create(request);
    }

    @Test
    void invalidMinimumAge() throws Exception {
        String body = EVENT_JSON.replace(
                "\"minimumAge\": 18",
                "\"minimumAge\": -1"
        );

        mvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.minimumAge").isNotEmpty());

        verifyNoInteractions(service);
    }

    @Test
    void invalidDescription() throws Exception {
        String body = EVENT_JSON.replace(
                "Music festival",
                "x".repeat(5001)
        );

        mvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.description").isNotEmpty());

        verifyNoInteractions(service);
    }

    @Test
    void invalidEventCode() throws Exception {
        String body = EVENT_JSON.replace("CMF-2026", " ");

        mvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.eventCode").isNotEmpty());

        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{",
            "",
            "null",
            "{\"category\":\"UNKNOWN\"}",
            "{\"eventDate\":\"not-a-date\"}",
            "{\"minimumAge\":{}}"
    })
    void unreadableBodyReturns400(String body) throws Exception {
        mvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON or invalid request body"))
                .andExpect(jsonPath("$.details").isMap());

        verifyNoInteractions(service);
    }

    @Test
    void missingQueryParameterReturns400() throws Exception {
        mvc.perform(get("/api/events/by-artist"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.stageName").value("Parameter is required"));

        verifyNoInteractions(service);
    }

    @Test
    void unsupportedContentTypeKeeps415() throws Exception {
        mvc.perform(post("/api/events").contentType(MediaType.TEXT_PLAIN).content(EVENT_JSON))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.details").isMap());

        verifyNoInteractions(service);
    }

    @Test
    void unsupportedMethodKeeps405() throws Exception {
        mvc.perform(delete("/api/events/CMF-2026"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status").value(405));

        verifyNoInteractions(service);
    }

    @Test
    void publishedEmptyCollectionIs200() throws Exception {
        when(service.findPublishedEvents()).thenReturn(List.of());

        mvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(service).findPublishedEvents();
    }
}