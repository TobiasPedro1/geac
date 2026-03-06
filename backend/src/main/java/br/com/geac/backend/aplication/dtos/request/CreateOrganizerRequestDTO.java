package br.com.geac.backend.aplication.dtos.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrganizerRequestDTO(
        @NotNull(message = "ID do usuário é obrigatório") UUID userId,
        @NotNull(message = "ID da organização é obrigatório") UUID organizerId,
        String justification
) {
}