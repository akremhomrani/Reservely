package tn.reservely.backend.features.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleTokenRequest(@NotBlank String idToken) {}
