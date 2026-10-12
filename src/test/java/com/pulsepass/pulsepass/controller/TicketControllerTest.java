package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.domain.TicketStatus;
import com.pulsepass.pulsepass.domain.TicketType;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TicketService service;

    private static final LocalDateTime PURCHASE_DATE = LocalDateTime.of(2030, 10, 3, 20, 0);

    private static final String TICKET_JSON = """
            {
              "userEmail": "andrea@example.com",
              "eventCode": "CMF-2026",
              "type": "GENERAL"
            }
            """;

    private static TicketResponse ticket(TicketStatus status) {
        return new TicketResponse(
                1L,
                "TKT-001",
                TicketType.GENERAL,
                new BigDecimal("100000.00"),
                status,
                PURCHASE_DATE,
                "andrea@example.com",
                "CMF-2026",
                "Caribbean Music Fest 2026"
        );
    }

    private static PurchaseTicketRequest validRequest() {
        return new PurchaseTicketRequest(
                "andrea@example.com",
                "CMF-2026",
                TicketType.GENERAL
        );
    }

    @Test
    void purchaseTicket() throws Exception {
        PurchaseTicketRequest request = validRequest();

        when(service.purchase(request))
                .thenReturn(ticket(TicketStatus.PAID));

        mvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TICKET_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.type").value("GENERAL"))
                .andExpect(jsonPath("$.userEmail").value("andrea@example.com"
                ));

        verify(service).purchase(request);
    }

    @Test
    void requiredFieldsRejected() throws Exception {
        mvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.userEmail").isNotEmpty())
                .andExpect(jsonPath("$.details.eventCode").isNotEmpty())
                .andExpect(jsonPath("$.details.type").isNotEmpty());

        verify(service, never()).purchase(any());
        verifyNoInteractions(service);
    }

    @Test
    void invalidUserEmail() throws Exception {
        String body = TICKET_JSON.replace(
                "andrea@example.com",
                "invalid-email"
        );

        mvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.userEmail").value("User email must be valid"));

        verify(service, never()).purchase(any());
        verifyNoInteractions(service);
    }

    @Test
    void missingUserOnPurchase() throws Exception {
        PurchaseTicketRequest request = validRequest();

        when(service.purchase(request))
                .thenThrow(new ResourceNotFoundException("User not found: andrea@example.com"));

        mvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TICKET_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: andrea@example.com"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).purchase(request);
    }

    @Test
    void purchaseBusinessConflict() throws Exception {
        PurchaseTicketRequest request = validRequest();

        when(service.purchase(request))
                .thenThrow(new BusinessRuleException("User does not meet the minimum age"));

        mvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TICKET_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User does not meet the minimum age"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).purchase(request);
    }

    @Test
    void findTicket() throws Exception {
        when(service.findByCode("TKT-001")).thenReturn(ticket(TicketStatus.PAID));

        mvc.perform(get("/api/tickets/TKT-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(service).findByCode("TKT-001");
    }

    @Test
    void missingTicket() throws Exception {
        when(service.findByCode("missing"))
                .thenThrow(new ResourceNotFoundException("Ticket not found: missing"));

        mvc.perform(get("/api/tickets/missing"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ticket not found: missing"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findByCode("missing");
    }

    @Test
    void ticketsByUser() throws Exception {
        when(service.findByUserEmail("andrea@example.com")).thenReturn(List.of(ticket(TicketStatus.PAID)));

        mvc.perform(get("/api/tickets/by-user")
                .param("email", "andrea@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ticketCode").value("TKT-001"))
                .andExpect(jsonPath("$[0].userEmail").value(
                        "andrea@example.com"
                ));

        verify(service).findByUserEmail("andrea@example.com");
    }

    @Test
    void paidTicketsByEvent() throws Exception {
        when(service.findPaidTicketsByEvent("CMF-2026")).thenReturn(List.of(ticket(TicketStatus.PAID)));

        mvc.perform(get("/api/events/CMF-2026/tickets/paid"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PAID"));

        verify(service).findPaidTicketsByEvent("CMF-2026");
    }

    @Test
    void cancelTicket() throws Exception {
        when(service.cancel("TKT-001")).thenReturn(ticket(TicketStatus.CANCELLED));

        mvc.perform(patch("/api/tickets/TKT-001/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(service).cancel("TKT-001");
    }

    @Test
    void cancelConflict() throws Exception {
        when(service.cancel("TKT-001"))
                .thenThrow(new BusinessRuleException("Used tickets cannot be cancelled"));

        mvc.perform(patch("/api/tickets/TKT-001/cancel"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Used tickets cannot be cancelled"));

        verify(service).cancel("TKT-001");
    }

    @Test
    void markAsUsedTicket() throws Exception {
        when(service.markAsUsed("TKT-001")).thenReturn(ticket(TicketStatus.USED));

        mvc.perform(patch("/api/tickets/TKT-001/use"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"))
                .andExpect(jsonPath("$.status").value("USED"));

        verify(service).markAsUsed("TKT-001");
    }

    @Test
    void markAsUsedConflict() throws Exception {
        when(service.markAsUsed("TKT-001"))
                .thenThrow(new BusinessRuleException("Cancelled tickets cannot be used"));

        mvc.perform(patch("/api/tickets/TKT-001/use"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Cancelled tickets cannot be used"));

        verify(service).markAsUsed("TKT-001");
    }
}
