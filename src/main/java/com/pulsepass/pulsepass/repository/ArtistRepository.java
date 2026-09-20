package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.domain.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    
}