package br.com.geac.backend.aplication.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequirementRequestDTO(

        @NotBlank
        @Size(min = 5, max = 255)
        String description
) {
}
