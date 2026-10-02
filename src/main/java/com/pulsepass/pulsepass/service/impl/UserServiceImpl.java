package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.UserProfile;
import com.pulsepass.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.pulsepass.dto.response.UserResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.UserMapper;
import com.pulsepass.pulsepass.repository.UserProfileRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException(
                    "Username already exists: " + request.username()
            );
        }

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException(
                    "Email already exists: " + request.email()
            );
        }

        if (request.birthDate() != null
                && request.birthDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException(
                    "Birth date cannot be in the future"
            );
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        profile.setCity(request.city());
        profile.setBirthDate(request.birthDate());
        profile.setUser(savedUser);

        UserProfile savedProfile =
                userProfileRepository.save(profile);

        savedUser.setProfile(savedProfile);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse findByEmail(String email) {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse findByUsername(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with username: " + username
                        )
                );

        return userMapper.toResponse(user);
    }
}