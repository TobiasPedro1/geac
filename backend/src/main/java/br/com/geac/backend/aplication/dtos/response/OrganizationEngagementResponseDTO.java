package br.com.geac.backend.aplication.dtos.response;

import java.util.UUID;

public record OrganizationEngagementResponseDTO(
        UUID organizerId,
        String organizerName,
        Long totalEventosRealizados,
        Long totalParticipantesEngajados
) {
}
