package br.com.geac.backend.aplication.dtos.response;

import java.util.UUID;

public record EventStatisticsResponseDTO(
        UUID eventId,
        String eventTitle,
        String eventStatus,
        Long totalInscritos,
        Long totalPresentes,
        Double mediaAvaliacao
) {
}
