package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.TagResponseDTO;
import br.com.geac.backend.aplication.dtos.request.TagRequestDTO;
import br.com.geac.backend.aplication.mappers.TagMapper;
import br.com.geac.backend.domain.entities.Tag;
import br.com.geac.backend.domain.exceptions.TagNotFoundException;
import br.com.geac.backend.infrastucture.repositories.TagRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock private TagRepository tagRepository;
    @Mock private TagMapper mapper;

    @InjectMocks
    private TagService tagService;

    private Tag tag;
    private TagResponseDTO tagResponse;
    private TagRequestDTO tagRequest;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setId(1);
        tag.setName("Tecnologia");

        tagResponse = new TagResponseDTO(1, "Tecnologia");
        tagRequest = new TagRequestDTO("Tecnologia");
    }

    @Test
    @DisplayName("Deve criar tag com sucesso")
    void createTag_Success() {
        when(mapper.toEntity(tagRequest)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(mapper.toDTO(tag)).thenReturn(tagResponse);

        TagResponseDTO result = tagService.createTag(tagRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Tecnologia");
        verify(tagRepository).save(tag);
    }

    @Test
    @DisplayName("Deve retornar tag por ID")
    void getById_Success() {
        when(tagRepository.findById(1)).thenReturn(Optional.of(tag));
        when(mapper.toDTO(tag)).thenReturn(tagResponse);

        TagResponseDTO result = tagService.getById(1);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve lancar excecao quando tag nao encontrada")
    void getById_NotFound_ThrowsException() {
        when(tagRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getById(99))
                .isInstanceOf(TagNotFoundException.class)
                .hasMessage("Tag not found");
    }

    @Test
    @DisplayName("Deve retornar todas as tags")
    void getAll_Success() {
        when(tagRepository.findAll()).thenReturn(List.of(tag));
        when(mapper.toDTO(tag)).thenReturn(tagResponse);

        List<TagResponseDTO> result = tagService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao ha tags")
    void getAll_EmptyList() {
        when(tagRepository.findAll()).thenReturn(List.of());

        List<TagResponseDTO> result = tagService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar tag com sucesso")
    void updateTag_Success() {
        TagRequestDTO updateRequest = new TagRequestDTO("IA");
        when(tagRepository.findById(1)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(mapper.toDTO(tag)).thenReturn(new TagResponseDTO(1, "IA"));

        TagResponseDTO result = tagService.updateSpeaker(1, updateRequest);

        assertThat(result).isNotNull();
        verify(tagRepository).save(tag);
    }

    @Test
    @DisplayName("Deve lancar excecao ao atualizar tag inexistente")
    void updateTag_NotFound_ThrowsException() {
        TagRequestDTO updateRequest = new TagRequestDTO("x");
        when(tagRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.updateSpeaker(99, updateRequest))
                .isInstanceOf(TagNotFoundException.class);
    }

    @Test
    @DisplayName("Deve deletar tag com sucesso")
    void deleteTag_Success() {
        when(tagRepository.findById(1)).thenReturn(Optional.of(tag));

        assertThatCode(() -> tagService.deleteTag(1)).doesNotThrowAnyException();

        verify(tagRepository).delete(tag);
    }

    @Test
    @DisplayName("Deve lancar excecao ao deletar tag inexistente")
    void deleteTag_NotFound_ThrowsException() {
        when(tagRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.deleteTag(99))
                .isInstanceOf(TagNotFoundException.class);
    }
}

