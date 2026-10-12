package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.dto.response.VenueResponse;
import com.pulsepass.pulsepass.service.VenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService service;

    public VenueController(VenueService service) {
        this.service = service;
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<VenueResponse>> findActiveVenues() {
        return ResponseEntity.ok(service.findActiveVenues());
    }

    @GetMapping("/{code}")
    public ResponseEntity<VenueResponse> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }


}
