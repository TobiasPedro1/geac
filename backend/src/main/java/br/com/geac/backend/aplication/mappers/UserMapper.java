package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.RegisterResponseDTO;
import br.com.geac.backend.aplication.dtos.request.RegisterRequestDTO;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User registerToUser(RegisterRequestDTO registerRequestDTO);

    default Role mapStringToRole(String role) {
        if (role == null) return null;
        return Role.valueOf(role.toUpperCase());
    }

    @Mapping(target = "message", constant = "User registered successfully")
    RegisterResponseDTO userToRegisterResponse(User user);
}