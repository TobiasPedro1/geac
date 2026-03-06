package br.com.geac.backend.aplication.dtos.response;

import java.util.UUID;

public record RegistrationResponseDTO(
        UUID userId,
        String userName,
        String userEmail,
        Boolean attended,
        String status
) {
}