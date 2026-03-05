package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.RequirementsResponseDTO;
import br.com.geac.backend.aplication.dtos.request.RequirementRequestDTO;
import br.com.geac.backend.domain.entities.EventRequirement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RequirementMapper {

    EventRequirement toEntity(RequirementRequestDTO dto);

    RequirementsResponseDTO toDTO(EventRequirement entity);
}
