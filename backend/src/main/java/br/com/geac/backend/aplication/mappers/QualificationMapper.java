package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.QualificationResponseDTO;
import br.com.geac.backend.aplication.dtos.request.QualificationRequestDTO;
import br.com.geac.backend.domain.entities.Qualification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QualificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "speaker", ignore = true)
    Qualification toEntity(QualificationRequestDTO dto);

    QualificationResponseDTO toDTO(Qualification entity);
}
