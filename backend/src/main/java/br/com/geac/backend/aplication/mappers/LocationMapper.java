package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.LocationResponseDTO;
import br.com.geac.backend.aplication.dtos.request.LocationPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.LocationRequestDTO;
import br.com.geac.backend.domain.entities.Location;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationResponseDTO toDto(Location location);

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationRequestDTO locationRequestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(LocationPatchRequestDTO dto, @MappingTarget Location location);
}
