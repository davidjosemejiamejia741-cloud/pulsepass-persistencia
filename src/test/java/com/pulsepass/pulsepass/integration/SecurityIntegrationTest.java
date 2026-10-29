package com.pulsepass.pulsepass.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pulsepass.pulsepass.dto.request.*;
import com.pulsepass.pulsepass.security.*;
import com.pulsepass.pulsepass.service.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SecurityIntegrationTest {
  @Container static PostgreSQLContainer db = new PostgreSQLContainer("postgres:18-alpine");
  static String secret =
      Base64.getEncoder().encodeToString(io.jsonwebtoken.Jwts.SIG.HS256.key().build().getEncoded());

  @DynamicPropertySource
  static void config(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", db::getJdbcUrl);
    r.add("spring.datasource.username", db::getUsername);
    r.add("spring.datasource.password", db::getPassword);
    r.add("pulsepass.security.jwt.secret-base64", () -> secret);
  }

  @Autowired AuthService auth;
  @Autowired JdbcTemplate jdbc;
  @Autowired PasswordEncoder encoder;
  @Autowired MockMvc mvc;

  RegisterUserRequest request(String name) {
    return new RegisterUserRequest(
        name,
        name + "@test.invalid",
        "Test",
        "User",
        null,
        null,
        LocalDate.of(1990, 1, 1),
        "OnlyForTests2026!");
  }

  @Test
  void registerLoginIdorAndDisabled() throws Exception {
    String name = "u" + UUID.randomUUID();
    var one = auth.register(request(name));
    var other = auth.register(request(name + "b"));
    String hash =
        jdbc.queryForObject(
            "select password_hash from user_credentials where user_id=?", String.class, one.id());
    assertThat(encoder.matches("OnlyForTests2026!", hash)).isTrue();
    assertThat(hash).isNotEqualTo("OnlyForTests2026!");
    assertThat(
            jdbc.queryForList(
                "select role from user_roles where user_id=?", String.class, one.id()))
        .containsExactly("USER");
    String token = auth.login(new LoginRequest(one.email(), "OnlyForTests2026!")).accessToken();
    mvc.perform(
            get("/api/users/by-email")
                .param("email", other.email())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/users/by-email")
                .param("email", one.email())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.password").doesNotExist());
    mvc.perform(
            post("/api/tickets")
                .contentType("application/json")
                .content(
                    "{\"userEmail\":\""
                        + other.email()
                        + "\",\"eventCode\":\"CMF-2026\",\"type\":\"GENERAL\"}")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    jdbc.update("update users set active=false where id=?", one.id());
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void rollbackWhenCredentialInsertFails() {
    jdbc.execute("ALTER TABLE user_credentials ADD CONSTRAINT test_reject CHECK (false) NOT VALID");
    String name = "rollback" + UUID.randomUUID();
    try {
      assertThatThrownBy(() -> auth.register(request(name))).isInstanceOf(RuntimeException.class);
      assertThat(
              jdbc.queryForObject("select count(*) from users where username=?", Long.class, name))
          .isZero();
    } finally {
      jdbc.execute("ALTER TABLE user_credentials DROP CONSTRAINT test_reject");
    }
  }

  @Test
  void duplicateRoleRejected() {
    var u = auth.register(request("role" + UUID.randomUUID()));
    assertThatThrownBy(
            () -> jdbc.update("insert into user_roles(user_id,role) values (?, 'USER')", u.id()))
        .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
  }
}
