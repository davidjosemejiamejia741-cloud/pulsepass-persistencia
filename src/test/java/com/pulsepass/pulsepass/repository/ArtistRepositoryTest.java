package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.TestcontainersConfiguration;
import com.pulsepass.pulsepass.TestcontainersConfiguration;
import com.pulsepass.pulsepass.domain.Artist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

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
                artistRepository.findByStageName("Solar Beat");


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

}
