package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.CategoryResponseDTO;
import br.com.geac.backend.aplication.dtos.request.CategoryPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.CategoryRequestDTO;
import br.com.geac.backend.aplication.mappers.CategoryMapper;
import br.com.geac.backend.domain.entities.Category;
import br.com.geac.backend.infrastucture.repositories.CategoryRepository;
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
class CategoryServiceTest {

    @Mock private CategoryRepository repository;
    @Mock private CategoryMapper mapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryResponseDTO categoryResponse;
    private CategoryRequestDTO categoryRequest;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1);
        category.setName("Palestra");

        categoryResponse = new CategoryResponseDTO(1, "Palestra", "DescriÃ§Ã£o da palestra");
        categoryRequest = new CategoryRequestDTO("Palestra", "DescriÃ§Ã£o da palestra com mais detalhes aqui");
    }

    @Test
    @DisplayName("Deve criar categoria com sucesso")
    void createCategory_Success() {
        when(mapper.toEntity(categoryRequest)).thenReturn(category);
        when(repository.save(category)).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponseDTO result = categoryService.createCategory(categoryRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Palestra");
        verify(repository).save(category);
    }

    @Test
    @DisplayName("Deve retornar categoria por ID")
    void getCategoryById_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(category));
        when(mapper.toResponse(category)).thenReturn(categoryResponse);

        CategoryResponseDTO result = categoryService.getCategoryById(1);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve lancar excecao quando categoria nao encontrada")
    void getCategoryById_NotFound_ThrowsException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
    }

    @Test
    @DisplayName("Deve retornar todas as categorias")
    void getAllCategory_Success() {
        when(repository.findAll()).thenReturn(List.of(category));
        when(mapper.toResponse(category)).thenReturn(categoryResponse);

        List<CategoryResponseDTO> result = categoryService.getAllCategory();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao ha categorias")
    void getAllCategory_EmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<CategoryResponseDTO> result = categoryService.getAllCategory();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar categoria com sucesso")
    void updateCategory_Success() {
        CategoryPatchRequestDTO patchDTO = new CategoryPatchRequestDTO("Workshop", "DescriÃ§Ã£o do workshop atualizada aqui");
        when(repository.findById(1)).thenReturn(Optional.of(category));
        when(repository.save(category)).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(new CategoryResponseDTO(1, "Workshop", "DescriÃ§Ã£o"));

        CategoryResponseDTO result = categoryService.updateCategory(1, patchDTO);

        assertThat(result).isNotNull();
        verify(mapper).updateEntityFromDto(patchDTO, category);
        verify(repository).save(category);
    }

    @Test
    @DisplayName("Deve lancar excecao ao atualizar categoria inexistente")
    void updateCategory_NotFound_ThrowsException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99, new CategoryPatchRequestDTO("x", "descriÃ§Ã£o longa aqui mesmo")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve deletar categoria com sucesso")
    void deleteCategory_Success() {
        when(repository.findById(1)).thenReturn(Optional.of(category));

        assertThatCode(() -> categoryService.deleteCategory(1)).doesNotThrowAnyException();

        verify(repository).delete(category);
    }

    @Test
    @DisplayName("Deve lancar excecao ao deletar categoria inexistente")
    void deleteCategory_NotFound_ThrowsException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99))
                .isInstanceOf(RuntimeException.class);
    }
}

