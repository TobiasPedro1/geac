package br.com.geac.backend.aplication.dtos.response;

import br.com.geac.backend.domain.enums.DaysBeforeNotify;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder

public record EventResponseDTO(
        UUID id,
        String title,
        String description,
        String onlineLink,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer workloadHours,
        Integer maxCapacity,
        String status,
        LocalDateTime createdAt,

        Integer categoryId,
        String categoryName,

        LocationResponseDTO location,

        String organizerName,
        String organizerEmail,

        List<RequirementsResponseDTO> requirements,

        List<String> tags,
        List<String> speakers,

        Integer registeredCount,
        Boolean isRegistered,
        String userRegistrationStatus,
        Boolean userAttended,

        DaysBeforeNotify daysBeforeNotify

) {
}
