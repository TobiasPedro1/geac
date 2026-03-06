package br.com.geac.backend.aplication.dtos.response;

public record UserRegistrationContextResponseDTO(
        Boolean isRegistered,
        String status,
        Boolean attended
) {
}
