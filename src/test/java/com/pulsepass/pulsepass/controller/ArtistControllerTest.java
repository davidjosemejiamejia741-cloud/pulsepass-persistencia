package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.ArtistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.context.annotation.Import(com.pulsepass.pulsepass.security.MvcSecuritySupport.class)
@org.springframework.security.test.context.support.WithMockUser(roles={"USER","STAFF","ADMIN"})
@WebMvcTest(ArtistController.class)
class ArtistControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ArtistService service;

    private static final ArtistResponse ARTIST = new ArtistResponse(
            1L,
            "Solar Beat",
            "Colombia",
            "Electronic",
            true
    );

    @Test
    void artistById() throws Exception {
        when(service.findById(1L)).thenReturn(ARTIST);

        mvc.perform(get("/api/artists/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(service).findById(1L);
    }

    @Test
    void missingArtist() throws Exception {
        when(service.findById(99L))
                .thenThrow(new ResourceNotFoundException("Artist not found: 99"));

        mvc.perform(get("/api/artists/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Artist not found: 99"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findById(99L);
    }

    @Test
    void artistByStageName() throws Exception {
        when(service.findByStageName("Solar Beat")).thenReturn(ARTIST);

        mvc.perform(get("/api/artists/by-stage-name")
                .param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(service).findByStageName("Solar Beat");
    }

    @Test
    void activeArtists() throws Exception {
        when(service.findActiveArtists()).thenReturn(List.of(ARTIST));

        mvc.perform(get("/api/artists/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].stageName").value("Solar Beat"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(service).findActiveArtists();
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mvc.perform(get("/api/artists/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.id").value("Invalid parameter format"));

        verifyNoInteractions(service);
    }

    @Test
    void missingStageNameReturns404() throws Exception {
        when(service.findByStageName("Unknown"))
                .thenThrow(new ResourceNotFoundException("Artist not found: Unknown"));

        mvc.perform(get("/api/artists/by-stage-name")
                .param("stageName", "Unknown"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Artist not found: Unknown"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findByStageName("Unknown");
    }
}