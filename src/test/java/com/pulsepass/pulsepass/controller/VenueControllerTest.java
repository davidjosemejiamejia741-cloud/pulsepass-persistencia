package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.dto.response.VenueResponse;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VenueController.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VenueService service;

    private static final VenueResponse VENUE = new VenueResponse(
            1L,
            "VEN-SMR-01",
            "Marina Convention Center",
            "Santa Marta",
            "Marina",
            3,
            true
    );

    @Test
    void existingVenue() throws Exception {
        when(service.findByCode("VEN-SMR-01")).thenReturn(VENUE);

        mvc.perform(get("/api/venues/VEN-SMR-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.name").value("Marina Convention Center"))
                .andExpect(jsonPath("$.capacity").value(3));

        verify(service).findByCode("VEN-SMR-01");
    }

    @Test
    void missingVenue() throws Exception {
        when(service.findByCode("missing"))
                .thenThrow(new ResourceNotFoundException(
                        "Venue not found: missing"
                ));

        mvc.perform(get("/api/venues/missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Venue not found: missing"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findByCode("missing");
    }

    @Test
    void activeVenues() throws Exception {
        when(service.findActiveVenues()).thenReturn(List.of(VENUE));

        mvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(service).findActiveVenues();
    }

    @Test
    void unexpectedErrorIsSanitized() throws Exception {
        when(service.findByCode("VEN-SMR-01"))
                .thenThrow(new IllegalStateException(
                        "database password secret"
                ));

        mvc.perform(get("/api/venues/VEN-SMR-01"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.details").isMap())
                .andExpect(jsonPath("$.stackTrace").doesNotExist());

        verify(service).findByCode("VEN-SMR-01");
    }
}