package tn.reservely.backend.features.auth.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        String name,
        String email,
        String role,
        String phone,
        String photoUrl
) {
    public static AuthResponse of(String token, UUID id, String name, String email, String role, String phone, String photoUrl) {
        return new AuthResponse(token, "Bearer", id, name, email, role, phone, photoUrl);
    }
}
