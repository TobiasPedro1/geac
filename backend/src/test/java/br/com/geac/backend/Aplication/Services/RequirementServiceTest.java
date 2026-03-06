package br.com.geac.backend.Aplication.Services;

import br.com.geac.backend.Aplication.DTOs.Reponse.RequirementsResponseDTO;
import br.com.geac.backend.Aplication.DTOs.Request.RequirementRequestDTO;
import br.com.geac.backend.Aplication.Mappers.RequirementMapper;
import br.com.geac.backend.Domain.Entities.EventRequirement;
import br.com.geac.backend.Domain.Exceptions.RequirementNotFoundException;
import br.com.geac.backend.Infrastructure.Repositories.EventRequirementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequirementServiceTest {

    @Mock private EventRequirementRepository repository;
    @Mock private RequirementMapper mapper;

    @InjectMocks
    private RequirementService requirementService;

    private EventRequirement requirement;
    private RequirementsResponseDTO requirementResponse;
    private RequirementRequestDTO requirementRequest;

    @BeforeEach
    void setUp() {
        requirement = new EventRequirement();
        requirement.setId(1);
        requirement.setDescription("Laptop com Java 21 instalado");

        requirementResponse = new RequirementsResponseDTO(1, "Laptop com Java 21 instalado");
        requirementRequest = new RequirementRequestDTO("Laptop com Java 21 instalado");
    }

    @Test
    @DisplayName("Deve criar requisito com sucesso")
    void createRequirement_Success() {
        when(mapper.toEntity(requirementRequest)).thenReturn(requirement);
        when(repository.save(requirement)).thenReturn(requirement);
        when(mapper.toDTO(requirement)).thenReturn(requirementResponse);

        RequirementsResponseDTO result = requirementService.createRequirement(requirementRequest);

        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("Laptop com Java 21 instalado");
        verify(repository).save(requirement);
    }

    @Test
    @DisplayName("Deve retornar requisito por ID")
    void getById_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(requirement));
        when(mapper.toDTO(requirement)).thenReturn(requirementResponse);

        RequirementsResponseDTO result = requirementService.getById(1);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando requisito não encontrado")
    void getById_NotFound_ThrowsException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requirementService.getById(99))
                .isInstanceOf(RequirementNotFoundException.class)
                .hasMessage("Requirement not found");
    }

    @Test
    @DisplayName("Deve retornar todos os requisitos")
    void getAll_Success() {
        when(repository.findAll()).thenReturn(List.of(requirement));
        when(mapper.toDTO(requirement)).thenReturn(requirementResponse);

        List<RequirementsResponseDTO> result = requirementService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há requisitos")
    void getAll_EmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<RequirementsResponseDTO> result = requirementService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar requisito com sucesso quando description não é null")
    void updateRequirement_WithDescription_Success() {
        RequirementRequestDTO updateRequest = new RequirementRequestDTO("Nova descrição do requisito");
        when(repository.findById(1)).thenReturn(Optional.of(requirement));
        when(mapper.toDTO(requirement)).thenReturn(requirementResponse);

        RequirementsResponseDTO result = requirementService.updateRequirement(1, updateRequest);

        assertThat(result).isNotNull();
        assertThat(requirement.getDescription()).isEqualTo("Nova descrição do requisito");
    }

    @Test
    @DisplayName("Deve atualizar requisito sem alterar description quando null")
    void updateRequirement_NullDescription_DoesNotChange() {
        RequirementRequestDTO updateRequest = new RequirementRequestDTO(null);
        when(repository.findById(1)).thenReturn(Optional.of(requirement));
        when(mapper.toDTO(requirement)).thenReturn(requirementResponse);

        requirementService.updateRequirement(1, updateRequest);

        assertThat(requirement.getDescription()).isEqualTo("Laptop com Java 21 instalado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar requisito inexistente")
    void updateRequirement_NotFound_ThrowsException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requirementService.updateRequirement(99, requirementRequest))
                .isInstanceOf(RequirementNotFoundException.class);
    }

    @Test
    @DisplayName("Deve deletar requisito com sucesso")
    void deleteRequirement_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(requirement));

        assertThatCode(() -> requirementService.deleteRequirement(1)).doesNotThrowAnyException();

        verify(repository).delete(requirement);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar requisito inexistente")
    void deleteRequirement_NotFound_ThrowsException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requirementService.deleteRequirement(99))
                .isInstanceOf(RequirementNotFoundException.class);
    }
}