package com.pulsepass.pulsepass.dto.response;

import java.util.List;

public record AuthenticatedUserResponse(
    Long id, 
    String username,
    List<String> roles) {}
