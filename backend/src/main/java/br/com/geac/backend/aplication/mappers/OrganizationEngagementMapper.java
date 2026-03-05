package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.OrganizationEngagementResponseDTO;
import br.com.geac.backend.domain.entities.OrganizationEngagement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizationEngagementMapper {
    OrganizationEngagementResponseDTO toResponseDTO(OrganizationEngagement entity);
}
