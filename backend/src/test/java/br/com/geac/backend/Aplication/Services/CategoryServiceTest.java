package br.com.geac.backend.Aplication.Services;

import br.com.geac.backend.Aplication.DTOs.Reponse.CategoryResponseDTO;
import br.com.geac.backend.Aplication.DTOs.Request.CategoryPatchRequestDTO;
import br.com.geac.backend.Aplication.DTOs.Request.CategoryRequestDTO;
import br.com.geac.backend.Aplication.Mappers.CategoryMapper;
import br.com.geac.backend.Domain.Entities.Category;
import br.com.geac.backend.Infrastructure.Repositories.CategoryRepository;
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

        categoryResponse = new CategoryResponseDTO(1, "Palestra", "Descrição da palestra");
        categoryRequest = new CategoryRequestDTO("Palestra", "Descrição da palestra com mais detalhes aqui");
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
    @DisplayName("Deve lançar exceção quando categoria não encontrada")
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
    @DisplayName("Deve retornar lista vazia quando não há categorias")
    void getAllCategory_EmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<CategoryResponseDTO> result = categoryService.getAllCategory();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar categoria com sucesso")
    void updateCategory_Success() {
        CategoryPatchRequestDTO patchDTO = new CategoryPatchRequestDTO("Workshop", "Descrição do workshop atualizada aqui");
        when(repository.findById(1)).thenReturn(Optional.of(category));
        when(repository.save(category)).thenReturn(category);
        when(mapper.toResponse(category)).thenReturn(new CategoryResponseDTO(1, "Workshop", "Descrição"));

        CategoryResponseDTO result = categoryService.updateCategory(1, patchDTO);

        assertThat(result).isNotNull();
        verify(mapper).updateEntityFromDto(patchDTO, category);
        verify(repository).save(category);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar categoria inexistente")
    void updateCategory_NotFound_ThrowsException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99, new CategoryPatchRequestDTO("x", "descrição longa aqui mesmo")))
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
    @DisplayName("Deve lançar exceção ao deletar categoria inexistente")
    void deleteCategory_NotFound_ThrowsException() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99))
                .isInstanceOf(RuntimeException.class);
    }
}