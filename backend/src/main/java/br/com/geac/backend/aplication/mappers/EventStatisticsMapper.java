package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.EventStatisticsResponseDTO;
import br.com.geac.backend.domain.entities.EventStatistics;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventStatisticsMapper {
    EventStatisticsResponseDTO toResponseDTO(EventStatistics entity);
}
