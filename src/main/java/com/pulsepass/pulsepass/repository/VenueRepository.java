package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.domain.Venue;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
List<Venue> findByCity(String city);
}