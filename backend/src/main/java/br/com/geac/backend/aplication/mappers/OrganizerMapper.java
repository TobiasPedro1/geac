package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.OrganizerResponseDTO;
import br.com.geac.backend.aplication.dtos.request.OrganizerRequestDTO;
import br.com.geac.backend.domain.entities.Organizer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizerMapper {
    OrganizerResponseDTO toResponseDTO(Organizer organizer);

    Organizer toEntity(OrganizerRequestDTO dto);
}