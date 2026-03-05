package br.com.geac.backend.aplication.dtos.response;

import java.util.Set;

public record SpeakerResponseDTO(
        Integer id,
        String name,
        String bio,
        String email,
        Set<QualificationResponseDTO> qualifications
) {
}
