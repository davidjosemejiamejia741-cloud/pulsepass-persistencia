package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService service;

    private static final UserResponse USER = new UserResponse(
            1L,
            "andrea",
            "andrea@example.com",
            true,
            "Andrea",
            "Perez",
            null,
            "Santa Marta",
            LocalDate.of(1990, 1, 1)
    );

    private static final String USER_JSON = """
            {
              "username": "andrea",
              "email": "andrea@example.com",
              "firstName": "Andrea",
              "lastName": "Perez",
              "city": "Santa Marta",
              "birthDate": "1990-01-01"
            }
            """;

    private static RegisterUserRequest validRequest() {
        return new RegisterUserRequest(
                "andrea",
                "andrea@example.com",
                "Andrea",
                "Perez",
                null,
                "Santa Marta",
                LocalDate.of(1990, 1, 1)
        );
    }

    @Test
    void registerUser() throws Exception {
        RegisterUserRequest request = validRequest();

        when(service.register(request)).thenReturn(USER);

        mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(USER_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@example.com"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).register(request);
    }

    @Test
    void requiredFieldsRejected() throws Exception {
        mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.username").isNotEmpty())
                .andExpect(jsonPath("$.details.email").isNotEmpty())
                .andExpect(jsonPath("$.details.firstName").isNotEmpty())
                .andExpect(jsonPath("$.details.lastName").isNotEmpty())
                .andExpect(jsonPath("$.details.birthDate").isNotEmpty());

        verify(service, never()).register(any());
        verifyNoInteractions(service);
    }

    @Test
    void invalidEmail() throws Exception {
        String body = USER_JSON.replace(
                "andrea@example.com",
                "invalid-email"
        );

        mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.email").value("Email must be valid"));

        verify(service, never()).register(any());
        verifyNoInteractions(service);
    }

    @Test
    void duplicateUsername() throws Exception {
        RegisterUserRequest request = validRequest();

        when(service.register(request))
                .thenThrow(new DuplicateResourceException(
                        "Username already exists: andrea"
                ));

        mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(USER_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username already exists: andrea"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).register(request);
    }

    @Test
    void userByEmail() throws Exception {
        when(service.findByEmail("andrea@example.com")).thenReturn(USER);

        mvc.perform(get("/api/users/by-email")
                .param("email", "andrea@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@example.com"));

        verify(service).findByEmail("andrea@example.com");
    }

    @Test
    void userByUsername() throws Exception {
        when(service.findByUsername("andrea")).thenReturn(USER);

        mvc.perform(get("/api/users/by-username")
                .param("username", "andrea"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.firstName").value("Andrea"));

        verify(service).findByUsername("andrea");
    }

    @Test
    void missingUser() throws Exception {
        when(service.findByEmail("missing@example.com"))
                .thenThrow(new ResourceNotFoundException("User not found: missing@example.com"));

        mvc.perform(get("/api/users/by-email")
                .param("email", "missing@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: missing@example.com"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findByEmail("missing@example.com");
    }
}
