package br.com.geac.backend.aplication.dtos.response;

import java.util.List;
import java.util.UUID;

public record OrganizerEventFeedbackResponseDTO(
        UUID eventId,
        String eventTitle,
        Double averageRating,
        Integer totalFeedbacks,
        List<EvaluationResponseDTO> feedbacks
) {
}
