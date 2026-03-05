package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.CategoryResponseDTO;
import br.com.geac.backend.aplication.dtos.request.CategoryPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.CategoryRequestDTO;
import br.com.geac.backend.aplication.mappers.CategoryMapper;
import br.com.geac.backend.domain.entities.Category;
import br.com.geac.backend.infrastucture.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        var category = mapper.toEntity(dto); // add data integrity to handler
        return mapper.toResponse(repository.save(category));
    }

    public CategoryResponseDTO getCategoryById(Integer id) {
        var category = getCategoryOrElseThrow(id);
        return mapper.toResponse(category);
    }

    public List<CategoryResponseDTO> getAllCategory() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Integer id, CategoryPatchRequestDTO dto) {
        var category = getCategoryOrElseThrow(id);
        mapper.updateEntityFromDto(dto, category);
        return mapper.toResponse(repository.save(category));
    }

    @Transactional
    public void deleteCategory(Integer id) {
        var category = getCategoryOrElseThrow(id);
        repository.delete(category);
    }

    private Category getCategoryOrElseThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
}
