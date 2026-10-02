package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.TestcontainersConfiguration;
import com.pulsepass.pulsepass.domain.Artist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ArtistRepositoryTest {


    @Autowired
    private ArtistRepository artistRepository;


    @Test
    void shouldFindArtistByStageName() {

        Optional<Artist> result =
                artistRepository.findByStageNameIgnoreCase("Solar Beat");


        assertTrue(result.isPresent());

        assertEquals(
                "Solar Beat",
                result.get().getStageName()
        );

    }

    @Test
    void shouldRejectDuplicatedStageName() {
        artistRepository.saveAndFlush(new Artist("Unique Artist", "Colombia", "Pop", true));

        assertThrows(DataIntegrityViolationException.class, () ->
                artistRepository.saveAndFlush(new Artist("Unique Artist", "Mexico", "Rock", true)));
    }

    @Test
    void shouldApplyInitialArtistMigration() {
        Set<String> stageNames = artistRepository.findAll().stream()
                .map(Artist::getStageName)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(stageNames.containsAll(Set.of(
                "Solar Beat", "Neon Waves", "Caribbean Sound", "Ocean Drive", "Digital Pulse")));
    }

    @Test
    void shouldSearchArtistsByNameIgnoringCase() {
        List<Artist> artists = artistRepository.searchByName("BEAT");

        assertEquals(List.of("Solar Beat"), artists.stream().map(Artist::getStageName).toList());
    }

}
