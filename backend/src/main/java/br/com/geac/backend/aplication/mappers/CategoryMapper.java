package br.com.geac.backend.aplication.mappers;

import br.com.geac.backend.aplication.dtos.response.CategoryResponseDTO;
import br.com.geac.backend.aplication.dtos.request.CategoryPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.CategoryRequestDTO;
import br.com.geac.backend.domain.entities.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDTO toResponse(Category category);

    Category toEntity(CategoryRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CategoryPatchRequestDTO dto, @MappingTarget Category entity);
}
