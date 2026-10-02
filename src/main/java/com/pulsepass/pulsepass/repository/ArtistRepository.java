package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.domain.Artist;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByStageNameIgnoreCase(String stageName);
    List<Artist> findByActiveTrueOrderByStageNameAsc();
    
    @Query("""
        SELECT a
        FROM Artist a
        WHERE LOWER(a.stageName)
        LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Artist> searchByName(String name);

}