package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.SpeakerResponseDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerRequestDTO;
import br.com.geac.backend.domain.entities.Speaker;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = QualificationMapper.class)
public interface SpeakerMapper {

    SpeakerResponseDTO toDto(Speaker speaker);

    @Mapping(target = "qualifications", ignore = true)
    @Mapping(target = "id", ignore = true)
    Speaker toEntity(SpeakerRequestDTO requestDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "qualifications", ignore = true)
    void updateEntityFromDTO(SpeakerPatchRequestDTO dto, @MappingTarget Speaker location);

}
