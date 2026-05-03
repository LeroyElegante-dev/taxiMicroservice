package com.taxi.common.dto;

import com.taxi.common.model.DriverStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DriverRegistrationRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 50) String phone,
        @NotBlank @Size(max = 100) String licenseNumber,
        DriverStatus status
) {
}
