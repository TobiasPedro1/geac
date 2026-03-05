package br.com.geac.backend.aplication.dtos.response;

import java.util.UUID;

public record OrganizerResponseDTO(
        UUID id,
        String name,
        String contactEmail
) {
}