package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.OrganizerResponseDTO;
import br.com.geac.backend.aplication.dtos.request.OrganizerRequestDTO;
import br.com.geac.backend.aplication.mappers.OrganizerMapper;
import br.com.geac.backend.domain.entities.Organizer;
import br.com.geac.backend.domain.entities.OrganizerMember;
import br.com.geac.backend.domain.exceptions.OrganizerAlreadyExists;
import br.com.geac.backend.domain.exceptions.OrganizerNotFoundExceptio;
import br.com.geac.backend.infrastucture.repositories.OrganizerMemberRepository;
import br.com.geac.backend.infrastucture.repositories.OrganizerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizerServiceTest {

    @Mock private OrganizerRepository organizerRepository;
    @Mock private OrganizerMapper organizerMapper;
    @Mock private OrganizerMemberRepository organizerMemberRepository;

    @InjectMocks
    private OrganizerService organizerService;

    private Organizer organizer;
    private OrganizerResponseDTO organizerResponse;
    private OrganizerRequestDTO organizerRequest;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();

        organizer = new Organizer();
        organizer.setId(orgId);
        organizer.setName("Org Teste");
        organizer.setContactEmail("org@teste.com");

        organizerResponse = new OrganizerResponseDTO(orgId, "Org Teste", "org@teste.com");
        organizerRequest = new OrganizerRequestDTO("Org Teste", "org@teste.com");
    }

    @Test
    @DisplayName("Deve criar organizacao com sucesso")
    void createOrganizer_Success() {
        when(organizerRepository.existsByName("Org Teste")).thenReturn(false);
        when(organizerMapper.toEntity(organizerRequest)).thenReturn(organizer);
        when(organizerRepository.save(organizer)).thenReturn(organizer);
        when(organizerMapper.toResponseDTO(organizer)).thenReturn(organizerResponse);

        OrganizerResponseDTO result = organizerService.createOrganizer(organizerRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Org Teste");
        verify(organizerRepository).save(organizer);
    }

    @Test
    @DisplayName("Deve lancar excecao quando organizacao ja existe")
    void createOrganizer_AlreadyExists_ThrowsException() {
        when(organizerRepository.existsByName("Org Teste")).thenReturn(true);

        assertThatThrownBy(() -> organizerService.createOrganizer(organizerRequest))
                .isInstanceOf(OrganizerAlreadyExists.class)
                .isInstanceOf(Exception.class);

        verify(organizerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar todas as organizacoes")
    void getAllOrganizers_Success() {
        when(organizerRepository.findAll()).thenReturn(List.of(organizer));
        when(organizerMapper.toResponseDTO(organizer)).thenReturn(organizerResponse);

        List<OrganizerResponseDTO> result = organizerService.getAllOrganizers();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao ha organizacoes")
    void getAllOrganizers_EmptyList() {
        when(organizerRepository.findAll()).thenReturn(List.of());

        List<OrganizerResponseDTO> result = organizerService.getAllOrganizers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar organizacao por ID")
    void getOrganizerById_Success() {
        when(organizerRepository.findById(orgId)).thenReturn(Optional.of(organizer));
        when(organizerMapper.toResponseDTO(organizer)).thenReturn(organizerResponse);

        OrganizerResponseDTO result = organizerService.getOrganizerById(orgId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(orgId);
    }

    @Test
    @DisplayName("Deve lancar excecao quando organizacao nao encontrada por ID")
    void getOrganizerById_NotFound_ThrowsException() {
        when(organizerRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizerService.getOrganizerById(UUID.randomUUID()))
                .isInstanceOf(OrganizerNotFoundExceptio.class);
    }

    @Test
    @DisplayName("Deve atualizar organizacao com sucesso")
    void updateOrganizer_Success() {
        OrganizerRequestDTO updateRequest = new OrganizerRequestDTO("Novo Nome", "novo@email.com");

        when(organizerRepository.findById(orgId)).thenReturn(Optional.of(organizer));
        when(organizerRepository.existsByName("Novo Nome")).thenReturn(false);
        when(organizerRepository.save(organizer)).thenReturn(organizer);
        when(organizerMapper.toResponseDTO(organizer)).thenReturn(organizerResponse);

        OrganizerResponseDTO result = organizerService.updateOrganizer(orgId, updateRequest);

        assertThat(result).isNotNull();
        verify(organizerRepository).save(organizer);
    }

    @Test
    @DisplayName("Deve lancar excecao ao atualizar com nome duplicado")
    void updateOrganizer_DuplicateName_ThrowsException() {
        OrganizerRequestDTO updateRequest = new OrganizerRequestDTO("Outro Nome", "email@test.com");

        when(organizerRepository.findById(orgId)).thenReturn(Optional.of(organizer));
        when(organizerRepository.existsByName("Outro Nome")).thenReturn(true);

        assertThatThrownBy(() -> organizerService.updateOrganizer(orgId, updateRequest))
                .isInstanceOf(OrganizerAlreadyExists.class);
    }

    @Test
    @DisplayName("Deve permitir atualizar mantendo o mesmo nome")
    void updateOrganizer_SameName_Success() {
        OrganizerRequestDTO updateRequest = new OrganizerRequestDTO("Org Teste", "novo@email.com");

        when(organizerRepository.findById(orgId)).thenReturn(Optional.of(organizer));
        when(organizerRepository.save(organizer)).thenReturn(organizer);
        when(organizerMapper.toResponseDTO(organizer)).thenReturn(organizerResponse);

        OrganizerResponseDTO result = organizerService.updateOrganizer(orgId, updateRequest);

        assertThat(result).isNotNull();
        verify(organizerRepository, never()).existsByName(any());
    }

    @Test
    @DisplayName("Deve deletar organizacao com sucesso")
    void deleteOrganizer_Success() {
        when(organizerRepository.findById(orgId)).thenReturn(Optional.of(organizer));

        assertThatCode(() -> organizerService.deleteOrganizer(orgId)).doesNotThrowAnyException();

        verify(organizerRepository).delete(organizer);
    }

    @Test
    @DisplayName("Deve lancar excecao ao deletar organizacao inexistente")
    void deleteOrganizer_NotFound_ThrowsException() {
        when(organizerRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizerService.deleteOrganizer(UUID.randomUUID()))
                .isInstanceOf(OrganizerNotFoundExceptio.class);
    }

    @Test
    @DisplayName("Deve retornar organizacoes do usuario")
    void getAllUserOrganizer_Success() {
        OrganizerMember member = new OrganizerMember();
        member.setOrganizer(organizer);

        when(organizerMemberRepository.getAllByUserId(any())).thenReturn(List.of(member));
        when(organizerRepository.findAllByIdIn(any())).thenReturn(List.of(organizer));
        when(organizerMapper.toResponseDTO(organizer)).thenReturn(organizerResponse);

        List<OrganizerResponseDTO> result = organizerService.getAllUserOrganizer(UUID.randomUUID());

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuario nao tem organizacoes")
    void getAllUserOrganizer_EmptyList() {
        when(organizerMemberRepository.getAllByUserId(any())).thenReturn(List.of());
        when(organizerRepository.findAllByIdIn(any())).thenReturn(List.of());

        List<OrganizerResponseDTO> result = organizerService.getAllUserOrganizer(UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}

