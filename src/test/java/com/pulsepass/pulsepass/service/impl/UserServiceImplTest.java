package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserProfile;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.mapper.UserMapper;
import com.pulsepass.pulsepass.repository.UserProfileRepository;
import com.pulsepass.pulsepass.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // TEST-USER-001
    @Test
    void shouldRegisterValidUser() {

        // ARRANGE
        RegisterUserRequest request =
                validRegisterRequest();

        UserResponse response =
                new UserResponse(
                        1L,
                        "andrea",
                        "andrea@email.com",
                        true,
                        "Andrea",
                        "Martinez",
                        "3001234567",
                        "Santa Marta",
                        LocalDate.of(2000, 5, 15)
                );

        when(
                userRepository.existsByUsername(
                        "andrea"
                )
        ).thenReturn(false);

        when(
                userRepository.existsByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(false);

        when(
                userRepository.save(
                        any(User.class)
                )
        ).thenAnswer(invocation -> {

            User user =
                    invocation.getArgument(0);

            user.setId(1L);

            return user;
        });

        when(
                userProfileRepository.save(
                        any(UserProfile.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(
                userMapper.toResponse(
                        any(User.class)
                )
        ).thenReturn(response);

        // ACT
        UserResponse result =
                userService.register(request);

        // ASSERT
        assertThat(result)
                .isEqualTo(response);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(
                        User.class
                );

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser =
                userCaptor.getValue();

        assertThat(savedUser.getUsername())
                .isEqualTo("andrea");

        assertThat(savedUser.getEmail())
                .isEqualTo("andrea@email.com");

        assertThat(savedUser.getActive())
                .isTrue();

        ArgumentCaptor<UserProfile> profileCaptor =
                ArgumentCaptor.forClass(
                        UserProfile.class
                );

        verify(userProfileRepository)
                .save(profileCaptor.capture());

        UserProfile savedProfile =
                profileCaptor.getValue();

        assertThat(savedProfile.getFirstName())
                .isEqualTo("Andrea");

        assertThat(savedProfile.getLastName())
                .isEqualTo("Martinez");

        assertThat(savedProfile.getPhone())
                .isEqualTo("3001234567");

        assertThat(savedProfile.getCity())
                .isEqualTo("Santa Marta");

        assertThat(savedProfile.getBirthDate())
                .isEqualTo(
                        LocalDate.of(2000, 5, 15)
                );

        assertThat(savedProfile.getUser())
                .isEqualTo(savedUser);
    }

    // TEST-USER-002
    @Test
    void shouldThrowDuplicateResourceExceptionWhenUsernameExists() {

        // ARRANGE
        RegisterUserRequest request =
                validRegisterRequest();

        when(
                userRepository.existsByUsername(
                        "andrea"
                )
        ).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(
                () -> userService.register(request)
        )
                .isInstanceOf(
                        DuplicateResourceException.class
                );

        verify(
                userRepository,
                never()
        ).save(any(User.class));

        verify(
                userProfileRepository,
                never()
        ).save(any(UserProfile.class));
    }

    // TEST-USER-003
    @Test
    void shouldThrowDuplicateResourceExceptionWhenEmailExists() {

        // ARRANGE
        RegisterUserRequest request =
                validRegisterRequest();

        when(
                userRepository.existsByUsername(
                        "andrea"
                )
        ).thenReturn(false);

        when(
                userRepository.existsByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(
                () -> userService.register(request)
        )
                .isInstanceOf(
                        DuplicateResourceException.class
                );

        verify(
                userRepository,
                never()
        ).save(any(User.class));

        verify(
                userProfileRepository,
                never()
        ).save(any(UserProfile.class));
    }

    // TEST-USER-004
    @Test
    void shouldThrowBusinessRuleExceptionWhenBirthDateIsInFuture() {

        // ARRANGE
        RegisterUserRequest request =
                new RegisterUserRequest(
                        "andrea",
                        "andrea@email.com",
                        "Andrea",
                        "Martinez",
                        "3001234567",
                        "Santa Marta",
                        LocalDate.now().plusDays(1)
                );

        when(
                userRepository.existsByUsername(
                        "andrea"
                )
        ).thenReturn(false);

        when(
                userRepository.existsByEmailIgnoreCase(
                        "andrea@email.com"
                )
        ).thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(
                () -> userService.register(request)
        )
                .isInstanceOf(
                        BusinessRuleException.class
                );

        verify(
                userRepository,
                never()
        ).save(any(User.class));

        verify(
                userProfileRepository,
                never()
        ).save(any(UserProfile.class));
    }

    private RegisterUserRequest validRegisterRequest() {

        return new RegisterUserRequest(
                "andrea",
                "andrea@email.com",
                "Andrea",
                "Martinez",
                "3001234567",
                "Santa Marta",
                LocalDate.of(2000, 5, 15)
        );
    }
}