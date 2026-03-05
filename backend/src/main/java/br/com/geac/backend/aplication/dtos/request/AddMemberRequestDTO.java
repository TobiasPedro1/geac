package br.com.geac.backend.aplication.dtos.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddMemberRequestDTO(
        @NotNull(message = "O ID do usuário é obrigatório")
        UUID userId
) {
}