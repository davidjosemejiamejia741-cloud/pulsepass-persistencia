package com.pulsepass.pulsepass.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final JwtProperties properties;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    if (properties.secretBase64() == null
        || properties.issuer() == null
        || properties.issuer().isBlank()
        || properties.accessTokenTtl() == null
        || properties.accessTokenTtl().toSeconds() <= 0)
      throw new IllegalArgumentException("Invalid JWT configuration");
    key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secretBase64()));
  }

  public long expiresIn() {
    return properties.accessTokenTtl().toSeconds();
  }

  public String issue(Long id) {
    Instant now = Instant.now();
    return Jwts.builder()
        .header()
        .type("JWT")
        .and()
        .subject(id.toString())
        .issuer(properties.issuer())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expiresIn())))
        .id(UUID.randomUUID().toString())
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public Long validate(String token) {
    var signed =
        Jwts.parser()
            .verifyWith(key)
            .requireIssuer(properties.issuer())
            .build()
            .parseSignedClaims(token);
    var c = signed.getPayload();
    var now = Instant.now();
    if (!"HS256".equals(signed.getHeader().getAlgorithm())
        || !"JWT".equals(signed.getHeader().getType())
        || c.getSubject() == null
        || c.getExpiration() == null
        || c.getIssuedAt() == null
        || c.getId() == null
        || c.getId().isBlank()
        || !c.getExpiration().toInstant().isAfter(now)
        || c.getIssuedAt().toInstant().isAfter(now.plusSeconds(30))
        || !c.getExpiration().after(c.getIssuedAt()))
      throw new MalformedJwtException("Invalid access token");
    try {
      long id = Long.parseLong(c.getSubject());
      if (id <= 0) throw new NumberFormatException();
      return id;
    } catch (NumberFormatException e) {
      throw new MalformedJwtException("Invalid subject");
    }
  }
}
