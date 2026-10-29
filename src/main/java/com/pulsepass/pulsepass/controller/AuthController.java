package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.dto.request.LoginRequest;
import com.pulsepass.pulsepass.dto.response.*;
import com.pulsepass.pulsepass.security.SecurityPrincipal;
import com.pulsepass.pulsepass.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return auth.login(request);
  }

  @GetMapping("/me")
  public AuthenticatedUserResponse me(@AuthenticationPrincipal SecurityPrincipal user) {
    return new AuthenticatedUserResponse(
        user.userId(),
        user.getUsername(),
        user.getAuthorities().stream().map(a -> a.getAuthority()).sorted().toList());
  }
}
