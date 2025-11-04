package br.ifsp.demo.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record AuthResponse(
        @Schema(description = "JTW credential")
        String token,

        @Schema(description = "Authenticated user details")
        UserInfo user
) {
    public static record UserInfo(
            @Schema(description = "User UUID")
            UUID id,
            @Schema(description = "User email")
            String email,
            @Schema(description = "Student ID (RA)")
            String studentId,
            @Schema(description = "User's full name")
            String name
    ) {}
}