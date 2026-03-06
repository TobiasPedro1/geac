package br.com.geac.backend.aplication.dtos.response;

import br.com.geac.backend.domain.enums.Campus;

public record LocationResponseDTO (
        Integer id,
        String name,
        String street,
        String number,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        Campus campus,
        String referencePoint,
        Integer capacity) {
}
