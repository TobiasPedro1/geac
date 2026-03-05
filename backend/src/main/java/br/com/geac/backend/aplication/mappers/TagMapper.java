package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.TagResponseDTO;
import br.com.geac.backend.aplication.dtos.request.TagRequestDTO;
import br.com.geac.backend.domain.entities.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {

    Tag toEntity(TagRequestDTO dto);

    TagResponseDTO toDTO(Tag tag);
}
