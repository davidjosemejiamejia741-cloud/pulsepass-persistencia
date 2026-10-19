package com.pulsepass.pulsepass.security;

import static org.assertj.core.api.Assertions.*;

import io.jsonwebtoken.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;

class JwtServiceTest {
  private final javax.crypto.SecretKey key = Jwts.SIG.HS256.key().build();

  private JwtService service() {
    return new JwtService(
        new JwtProperties(
            "pulsepass-api",
            Base64.getEncoder().encodeToString(key.getEncoded()),
            Duration.ofMinutes(15)));
  }

  private JwtBuilder builder() {
    return Jwts.builder()
        .header()
        .type("JWT")
        .and()
        .subject("1")
        .issuer("pulsepass-api")
        .issuedAt(new Date())
        .expiration(Date.from(Instant.now().plusSeconds(900)))
        .id(UUID.randomUUID().toString());
  }

  @Test
  void roundTrip() {
    var s = service();
    String token = s.issue(1L);
    assertThat(s.validate(token)).isEqualTo(1L);
    assertThat(s.expiresIn()).isEqualTo(900);
    var c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    assertThat(c.getId()).isNotBlank();
    assertThat(c.getExpiration().getTime() - c.getIssuedAt().getTime()).isEqualTo(900000);
  }

  @Test
  void expired() {
    assertThatThrownBy(
            () ->
                service()
                    .validate(
                        builder()
                            .expiration(Date.from(Instant.now().minusSeconds(60)))
                            .signWith(key)
                            .compact()))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void issuer() {
    assertThatThrownBy(() -> service().validate(builder().issuer("other").signWith(key).compact()))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void alteredSignature() {
    assertThatThrownBy(
            () -> service().validate(builder().signWith(Jwts.SIG.HS256.key().build()).compact()))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void blank() {
    assertThatThrownBy(() -> service().validate("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void weakKey() {
    assertThatThrownBy(
            () ->
                new JwtService(
                    new JwtProperties(
                        "api",
                        Base64.getEncoder().encodeToString(new byte[8]),
                        Duration.ofMinutes(15))))
        .isInstanceOf(io.jsonwebtoken.security.WeakKeyException.class);
  }

  @Test
  void missingExpiration() {
    assertThatThrownBy(() -> service().validate(builder().expiration(null).signWith(key).compact()))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void missingSubject() {
    assertThatThrownBy(() -> service().validate(builder().subject(null).signWith(key).compact()))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void futureIssuedAt() {
    assertThatThrownBy(
            () ->
                service()
                    .validate(
                        builder()
                            .issuedAt(Date.from(Instant.now().plusSeconds(120)))
                            .signWith(key)
                            .compact()))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void wrongAlgorithm() {
    var large = Jwts.SIG.HS512.key().build();
    var s =
        new JwtService(
            new JwtProperties(
                "pulsepass-api",
                Base64.getEncoder().encodeToString(large.getEncoded()),
                Duration.ofMinutes(15)));
    assertThatThrownBy(() -> s.validate(builder().signWith(large, Jwts.SIG.HS512).compact()))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void missingId() {
    assertThatThrownBy(() -> service().validate(builder().id(null).signWith(key).compact()))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void unsigned() {
    assertThatThrownBy(() -> service().validate(builder().compact()))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void passwordBytes() {
    var v = new PasswordValidator();
    assertThat(v.isValid("a".repeat(72), null)).isTrue();
    assertThat(v.isValid("é".repeat(37), null)).isFalse();
    assertThat(v.isValid("short", null)).isFalse();
  }
}
