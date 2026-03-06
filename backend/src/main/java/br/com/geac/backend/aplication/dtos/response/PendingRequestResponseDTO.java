package br.com.geac.backend.aplication.dtos.response;

import java.time.LocalDateTime;

public record PendingRequestResponseDTO(
        Integer id,
        String userName,
        String userEmail,
        String organizerName,
        String justification,
        LocalDateTime createdAt
) {
}