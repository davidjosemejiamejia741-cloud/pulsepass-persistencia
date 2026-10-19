package com.pulsepass.pulsepass.security;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pulsepass.pulsepass.controller.*;
import com.pulsepass.pulsepass.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({
  VenueController.class,
  ArtistController.class,
  EventController.class,
  UserController.class,
  TicketController.class,
  AuthController.class
})
@Import(MvcSecuritySupport.class)
class SecurityHttpTest {
  @Autowired MockMvc mvc;
  @Autowired JwtService jwt;
  @Autowired CustomUserDetailsService identities;
  @MockitoBean VenueService venues;
  @MockitoBean ArtistService artists;
  @MockitoBean EventService events;
  @MockitoBean UserService users;
  @MockitoBean TicketService tickets;
  @MockitoBean AuthService auth;

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/api/venues/active",
        "/api/venues/VEN",
        "/api/artists/active",
        "/api/artists/1",
        "/api/artists/by-stage-name?stageName=A",
        "/api/events/published",
        "/api/events/by-artist?stageName=A",
        "/api/events/EVT"
      })
  void publicGets(String path) throws Exception {
    mvc.perform(get(path)).andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/api/users/by-email?email=a@b.co",
        "/api/users/by-username?username=a",
        "/api/tickets/T",
        "/api/tickets/by-user?email=a@b.co",
        "/api/events/E/tickets/paid",
        "/api/auth/me"
      })
  void protectedGets(String path) throws Exception {
    mvc.perform(get(path))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401));
  }

  @Test
  void anonymousCreate() throws Exception {
    mvc.perform(post("/api/events")).andExpect(status().isUnauthorized());
    verifyNoInteractions(events);
  }

  @Test
  void userCannotCreate() throws Exception {
    mvc.perform(post("/api/events").with(user("a").roles("USER")))
        .andExpect(status().isForbidden());
    verifyNoInteractions(events);
  }

  @Test
  void adminPublish() throws Exception {
    mvc.perform(patch("/api/events/E/publish").with(user("a").roles("ADMIN")))
        .andExpect(status().isOk());
    verify(events).publish("E");
  }

  @Test
  void userCannotUse() throws Exception {
    mvc.perform(patch("/api/tickets/T/use").with(user("a").roles("USER")))
        .andExpect(status().isForbidden());
    verifyNoInteractions(tickets);
  }

  @Test
  void staffUse() throws Exception {
    mvc.perform(patch("/api/tickets/T/use").with(user("a").roles("STAFF")))
        .andExpect(status().isOk());
    verify(tickets).markAsUsed("T");
  }

  @Test
  void staffCannotCancel() throws Exception {
    mvc.perform(patch("/api/tickets/T/cancel").with(user("a").roles("STAFF")))
        .andExpect(status().isForbidden());
  }

  @Test
  void invalidToken() throws Exception {
    when(jwt.validate("invalid")).thenThrow(new io.jsonwebtoken.MalformedJwtException("internal"));
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer invalid"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.details").isEmpty());
  }

  @Test
  void duplicateHeader() throws Exception {
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer one", "Bearer two"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void queryTokenIgnored() throws Exception {
    mvc.perform(get("/api/auth/me?token=anything")).andExpect(status().isUnauthorized());
  }

  @Test
  void deactivated() throws Exception {
    when(jwt.validate("valid")).thenReturn(1L);
    when(identities.loadById(1L))
        .thenReturn(
            new SecurityPrincipal(1L, "a", "a@b.co", "hash", false, java.util.List.of("USER")));
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer valid"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void validBearerAndNoSession() throws Exception {
    when(jwt.validate("valid")).thenReturn(1L);
    when(identities.loadById(1L))
        .thenReturn(
            new SecurityPrincipal(1L, "a", "a@b.co", "hash", true, java.util.List.of("USER")));
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer valid"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(cookie().doesNotExist("JSESSIONID"));
  }

  static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> routeMatrix() {
    String[][] routes = {
      {"GET", "/api/venues/V", "PUBLIC"},
      {"GET", "/api/venues/active", "PUBLIC"},
      {"POST", "/api/events", "ADMIN"},
      {"GET", "/api/events/E", "PUBLIC"},
      {"GET", "/api/events/published", "PUBLIC"},
      {"PATCH", "/api/events/E/publish", "ADMIN"},
      {"POST", "/api/events/E/artists/1", "ADMIN"},
      {"GET", "/api/events/by-artist?stageName=A", "PUBLIC"},
      {"GET", "/api/artists/1", "PUBLIC"},
      {"GET", "/api/artists/by-stage-name?stageName=A", "PUBLIC"},
      {"GET", "/api/artists/active", "PUBLIC"},
      {"POST", "/api/users", "PUBLIC"},
      {"GET", "/api/users/by-email?email=a@b.co", "AUTH"},
      {"GET", "/api/users/by-username?username=a", "AUTH"},
      {"POST", "/api/tickets", "USER"},
      {"GET", "/api/tickets/T", "AUTH"},
      {"GET", "/api/tickets/by-user?email=a@b.co", "AUTH"},
      {"GET", "/api/events/E/tickets/paid", "STAFF,ADMIN"},
      {"PATCH", "/api/tickets/T/cancel", "USER,ADMIN"},
      {"PATCH", "/api/tickets/T/use", "STAFF,ADMIN"}
    };
    return java.util.Arrays.stream(routes)
        .flatMap(
            r ->
                java.util.stream.Stream.of("ANON", "USER", "STAFF", "ADMIN")
                    .map(
                        role ->
                            org.junit.jupiter.params.provider.Arguments.of(
                                r[0], r[1], r[2], role)));
  }

  @ParameterizedTest
  @org.junit.jupiter.params.provider.MethodSource("routeMatrix")
  void twentyRoutesFourIdentities(String method, String path, String policy, String role)
      throws Exception {
    var req = request(org.springframework.http.HttpMethod.valueOf(method), path);
    if (!role.equals("ANON")) req.with(user("matrix").roles(role));
    // Empty JSON intentionally tests that allowed requests reach Bean Validation.
    if (method.equals("POST") && !path.contains("/artists/"))
      req.contentType("application/json").content("{}");
    boolean allowed =
        policy.equals("PUBLIC")
            || (!role.equals("ANON")
                && (policy.equals("AUTH")
                    || java.util.Arrays.asList(policy.split(",")).contains(role)));
    int expected =
        allowed
            ? (method.equals("POST") && !path.contains("/artists/") ? 400 : 200)
            : (role.equals("ANON") ? 401 : 403);
    mvc.perform(req).andExpect(status().is(expected));
  }

  @Test
  void publicLoginContract() throws Exception {
    when(auth.login(any()))
        .thenReturn(
            new com.pulsepass.pulsepass.dto.response.AuthResponse("test-token", "Bearer", 900));
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"identifier\":\"andrea\",\"password\":\"TestPassword2026!\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresIn").value(900));
  }

  @Test
  void wrongLoginGenericError() throws Exception {
    when(auth.login(any()))
        .thenThrow(
            new org.springframework.security.authentication.BadCredentialsException(
                "internal detail"));
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"identifier\":\"missing\",\"password\":\"WrongPassword2026!\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Credenciales inválidas o autenticación requerida"))
        .andExpect(jsonPath("$.stacktrace").doesNotExist());
  }
}
