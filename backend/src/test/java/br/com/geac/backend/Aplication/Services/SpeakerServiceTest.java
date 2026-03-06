package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.request.QualificationRequestDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerRequestDTO;
import br.com.geac.backend.aplication.dtos.response.SpeakerResponseDTO;
import br.com.geac.backend.aplication.mappers.QualificationMapper;
import br.com.geac.backend.aplication.mappers.SpeakerMapper;
import br.com.geac.backend.domain.entities.Qualification;
import br.com.geac.backend.domain.entities.Speaker;
import br.com.geac.backend.domain.exceptions.SpeakerAlreadyExistsException;
import br.com.geac.backend.domain.exceptions.SpeakerNotFoundException;
import br.com.geac.backend.infrastucture.repositories.SpeakerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpeakerServiceTest {

    @Mock private SpeakerRepository repository;
    @Mock private SpeakerMapper mapper;
    @Mock private QualificationMapper qualificationMapper;

    @InjectMocks
    private SpeakerService service;

    private Speaker speaker;
    private SpeakerResponseDTO responseDTO;
    private SpeakerRequestDTO createDTO;

    @BeforeEach
    void setUp() {
        speaker = new Speaker();
        speaker.setId(1);
        speaker.setName("Speaker");
        speaker.setBio("Bio");
        speaker.setEmail("speaker@test.com");

        responseDTO = new SpeakerResponseDTO(1, "Speaker", "Bio", "speaker@test.com", Set.of());
        createDTO = new SpeakerRequestDTO(
                "Speaker",
                "Bio text",
                Set.of(new QualificationRequestDTO("MSc", "UFAPE")),
                "speaker@test.com"
        );
    }

    @Test
    @DisplayName("createSpeaker deve salvar palestrante mapeado")
    void createSpeaker_Success() {
        when(repository.existsByNameAndEmail("Speaker", "speaker@test.com")).thenReturn(false);
        when(mapper.toEntity(createDTO)).thenReturn(speaker);
        when(qualificationMapper.toEntity(any())).thenReturn(new Qualification());
        when(repository.save(speaker)).thenReturn(speaker);
        when(mapper.toDto(speaker)).thenReturn(responseDTO);

        SpeakerResponseDTO result = service.createSpeaker(createDTO);

        assertThat(result.name()).isEqualTo("Speaker");
        verify(repository).save(speaker);
    }

    @Test
    @DisplayName("createSpeaker deve lancar excecao quando existe duplicidade")
    void createSpeaker_Duplicate() {
        when(repository.existsByNameAndEmail("Speaker", "speaker@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createSpeaker(createDTO))
                .isInstanceOf(SpeakerAlreadyExistsException.class);
    }

    @Test
    @DisplayName("getById deve lancar excecao quando palestrante nao existe")
    void getById_NotFound() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1))
                .isInstanceOf(SpeakerNotFoundException.class);
    }

    @Test
    @DisplayName("getAll deve mapear todos os palestrantes")
    void getAll_Success() {
        when(repository.findAll()).thenReturn(List.of(speaker));
        when(mapper.toDto(speaker)).thenReturn(responseDTO);

        List<SpeakerResponseDTO> result = service.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("updateSpeaker deve lancar excecao quando duplicidade existe para outro id")
    void updateSpeaker_Duplicate() {
        SpeakerPatchRequestDTO patch = new SpeakerPatchRequestDTO("Name", "Bio", "dup@test.com", Set.of());
        when(repository.findById(1)).thenReturn(Optional.of(speaker));
        when(repository.existsByNameAndEmailAndIdNot(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.updateSpeaker(1, patch))
                .isInstanceOf(SpeakerAlreadyExistsException.class);
    }

    @Test
    @DisplayName("updateSpeaker deve atualizar qualificacoes quando houver mudanca")
    void updateSpeaker_QualificationsChanged() {
        Qualification oldQ = new Qualification();
        oldQ.setTitleName("Old");
        oldQ.setInstitution("UFAPE");
        speaker.setQualifications(Set.of(oldQ));
        SpeakerPatchRequestDTO patch = new SpeakerPatchRequestDTO(
                "Speaker",
                "Bio",
                "speaker@test.com",
                Set.of(new QualificationRequestDTO("New", "UFAPE"))
        );

        when(repository.findById(1)).thenReturn(Optional.of(speaker));
        when(repository.existsByNameAndEmailAndIdNot(any(), any(), any())).thenReturn(false);
        when(qualificationMapper.toEntity(any())).thenAnswer(inv -> {
            Qualification q = new Qualification();
            QualificationRequestDTO dto = inv.getArgument(0);
            q.setTitleName(dto.titleName());
            q.setInstitution(dto.institution());
            return q;
        });
        when(repository.save(speaker)).thenReturn(speaker);
        when(mapper.toDto(speaker)).thenReturn(responseDTO);

        assertThatCode(() -> service.updateSpeaker(1, patch)).doesNotThrowAnyException();

        verify(repository).save(speaker);
        assertThat(speaker.getQualifications())
                .extracting(Qualification::getTitleName)
                .contains("New");
    }

    @Test
    @DisplayName("updateSpeaker nao deve alterar qualificacoes quando lista do dto esta vazia")
    void updateSpeaker_QualificationsEmpty() {
        SpeakerPatchRequestDTO patch = new SpeakerPatchRequestDTO("Speaker", "Bio", "speaker@test.com", Set.of());
        speaker.setQualifications(Set.of(new Qualification()));

        when(repository.findById(1)).thenReturn(Optional.of(speaker));
        when(repository.existsByNameAndEmailAndIdNot(any(), any(), any())).thenReturn(false);
        when(repository.save(speaker)).thenReturn(speaker);
        when(mapper.toDto(speaker)).thenReturn(responseDTO);

        service.updateSpeaker(1, patch);

        verify(qualificationMapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("updateSpeaker nao deve resolver qualificacoes quando qualificacoes do dto e null")
    void updateSpeaker_QualificationsNull() {
        SpeakerPatchRequestDTO patch = new SpeakerPatchRequestDTO("Speaker", "Bio", "speaker@test.com", null);

        when(repository.findById(1)).thenReturn(Optional.of(speaker));
        when(repository.existsByNameAndEmailAndIdNot(any(), any(), any())).thenReturn(false);
        when(repository.save(speaker)).thenReturn(speaker);
        when(mapper.toDto(speaker)).thenReturn(responseDTO);

        service.updateSpeaker(1, patch);

        verify(qualificationMapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("updateSpeaker deve manter qualificacoes existentes quando nao houver mudanca")
    void updateSpeaker_QualificationsUnchanged() {
        Qualification existing = new Qualification();
        existing.setTitleName("MSc");
        existing.setInstitution("UFAPE");
        speaker.setQualifications(Set.of(existing));

        SpeakerPatchRequestDTO patch = new SpeakerPatchRequestDTO(
                "Speaker",
                "Bio",
                "speaker@test.com",
                Set.of(new QualificationRequestDTO("MSc", "UFAPE"))
        );

        when(repository.findById(1)).thenReturn(Optional.of(speaker));
        when(repository.existsByNameAndEmailAndIdNot(any(), any(), any())).thenReturn(false);
        when(repository.save(speaker)).thenReturn(speaker);
        when(mapper.toDto(speaker)).thenReturn(responseDTO);

        service.updateSpeaker(1, patch);

        verify(qualificationMapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("updateSpeaker deve tratar mesmo titulo e instituicao diferente como mudanca")
    void updateSpeaker_QualificationInstitutionDiffers() {
        Qualification existing = new Qualification();
        existing.setTitleName("MSc");
        existing.setInstitution("UFAPE");
        speaker.setQualifications(Set.of(existing));

        SpeakerPatchRequestDTO patch = new SpeakerPatchRequestDTO(
                "Speaker",
                "Bio",
                "speaker@test.com",
                Set.of(new QualificationRequestDTO("MSc", "Another"))
        );

        when(repository.findById(1)).thenReturn(Optional.of(speaker));
        when(repository.existsByNameAndEmailAndIdNot(any(), any(), any())).thenReturn(false);
        when(qualificationMapper.toEntity(any())).thenAnswer(inv -> {
            Qualification q = new Qualification();
            QualificationRequestDTO dto = inv.getArgument(0);
            q.setTitleName(dto.titleName());
            q.setInstitution(dto.institution());
            return q;
        });
        when(repository.save(speaker)).thenReturn(speaker);
        when(mapper.toDto(speaker)).thenReturn(responseDTO);

        service.updateSpeaker(1, patch);

        verify(qualificationMapper).toEntity(any(QualificationRequestDTO.class));
    }

    @Test
    @DisplayName("deleteSpeaker deve remover palestrante existente")
    void deleteSpeaker_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(speaker));

        service.deleteSpeaker(1);

        verify(repository).delete(speaker);
    }
}

