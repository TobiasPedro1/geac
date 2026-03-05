package br.com.geac.backend.aplication.dtos.response;

import java.time.LocalDateTime;

public record EvaluationResponseDTO(
        Long id,
        java.util.UUID registrationId,
        java.util.UUID eventId,
        String eventTitle,
        java.util.UUID userId,
        String userName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {

}
