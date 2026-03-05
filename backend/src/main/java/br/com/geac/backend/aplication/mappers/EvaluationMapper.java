package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.EvaluationResponseDTO;
import br.com.geac.backend.domain.entities.Evaluation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EvaluationMapper {

    @Mapping(source = "registration.id", target = "registrationId")
    @Mapping(source = "registration.event.id", target = "eventId")
    @Mapping(source = "registration.event.title", target = "eventTitle")
    @Mapping(source = "registration.user.id", target = "userId")
    @Mapping(source = "registration.user.name", target = "userName")
    EvaluationResponseDTO toDTO(Evaluation evaluationResponseDTO);
}
