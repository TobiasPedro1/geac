package br.com.geac.backend.aplication.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberResponseDTO(
        UUID userId,
        String name,
        String email,
        LocalDateTime joinedAt
) {
}