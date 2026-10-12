package com.pulsepass.pulsepass.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterUserRequest(

        @NotBlank(message = "Username is required")
        @Size(max = 100)
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150)
        String email,

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @Size(max = 50)
        String phone,

        @Size(max = 100)
        String city,

        @NotNull(message = "Birth date is required")
        LocalDate birthDate

) {
}
