package com.pulsepass.pulsepass.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("pulsepass.security.jwt")
public record JwtProperties(String issuer, String secretBase64, Duration accessTokenTtl) {}
