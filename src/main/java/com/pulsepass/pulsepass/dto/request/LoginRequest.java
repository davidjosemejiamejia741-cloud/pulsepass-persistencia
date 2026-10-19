package com.pulsepass.pulsepass.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
    @NotBlank @Size(max = 150)
    String identifier, 
    
    @NotBlank @Size(max = 72)
    String password   ){
  @Override
  public String toString() {
    return "LoginRequest[REDACTED]";
  }
}
