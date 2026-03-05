package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.NotificationResponseDTO;
import br.com.geac.backend.domain.entities.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponseDTO toDTO(Notification notification);
}
