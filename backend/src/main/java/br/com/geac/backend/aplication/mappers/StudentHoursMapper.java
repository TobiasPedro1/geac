package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.request.StudentHoursResponseDTO;
import br.com.geac.backend.domain.entities.StudentExtracurricularHours;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentHoursMapper {
    StudentHoursResponseDTO toResponseDTO(StudentExtracurricularHours entity);
}