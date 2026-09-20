package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.TestcontainersConfiguration;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;

    @Test
    void shouldFindUserIgnoringEmailCaseAndPersistProfile() {
        User user = userRepository.saveAndFlush(new User("laura", "Laura@Example.com", true, null, List.of()));
        UserProfile profile = userProfileRepository.saveAndFlush(new UserProfile("Laura", "Lopez", "3000000000",
                "Medellin", LocalDate.of(2000, 1, 1), user));

        assertEquals(user.getId(), userRepository.findByEmailIgnoreCase("laura@example.com").orElseThrow().getId());
        assertEquals(profile.getId(), userProfileRepository.findByUserId(user.getId()).orElseThrow().getId());
    }

    @Test
    void shouldRejectSecondProfileForTheSameUser() {
        User user = userRepository.saveAndFlush(new User("profile-user", "profile@example.com", true, null, List.of()));
        userProfileRepository.saveAndFlush(new UserProfile("First", "Profile", null, null, null, user));

        assertThrows(DataIntegrityViolationException.class, () -> userProfileRepository.saveAndFlush(
                new UserProfile("Second", "Profile", null, null, null, user)));
    }
}
