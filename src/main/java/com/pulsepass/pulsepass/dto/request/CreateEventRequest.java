package com.pulsepass.pulsepass.dto.request;

import java.time.LocalDateTime;

import com.pulsepass.pulsepass.domain.EventCategory;

public record CreateEventRequest(
    
        String eventCode,
        String name,
        String description,
        EventCategory category,
        LocalDateTime eventDate,
        Integer minimumAge,
        String venueCode

) {

}
