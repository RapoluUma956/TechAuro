package com.assetmanagement.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UserRequest(

        @NotBlank
        @Size(max = 50)
        String employeeCode,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone,

        String password,

        @Size(max = 100)
        String designation,

        LocalDate dateOfJoining
) {}
