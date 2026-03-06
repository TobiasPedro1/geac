package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.RequirementsResponseDTO;
import br.com.geac.backend.aplication.dtos.request.RequirementRequestDTO;
import br.com.geac.backend.aplication.mappers.RequirementMapper;
import br.com.geac.backend.domain.entities.EventRequirement;
import br.com.geac.backend.domain.exceptions.RequirementNotFoundException;
import br.com.geac.backend.infrastucture.repositories.EventRequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final EventRequirementRepository repository;
    private final RequirementMapper mapper;

    @Transactional
    public RequirementsResponseDTO createRequirement(RequirementRequestDTO dto) {

        var toBeSaved = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(toBeSaved));
    }

    public RequirementsResponseDTO getById(Integer id) {
        var requirement = getRequirementByIdOrThrowBadRequest(id);
        return mapper.toDTO(requirement);
    }

    public List<RequirementsResponseDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public RequirementsResponseDTO updateRequirement(Integer id, RequirementRequestDTO dto) {
        var requirement = getRequirementByIdOrThrowBadRequest(id);
        if (dto.description() != null) requirement.setDescription(dto.description());
        return mapper.toDTO(requirement);
    }

    @Transactional
    public void deleteRequirement(Integer id) {
        var requirement = getRequirementByIdOrThrowBadRequest(id);
        repository.delete(requirement);
    }

    private EventRequirement getRequirementByIdOrThrowBadRequest(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RequirementNotFoundException("Requirement not found"));
    }
}
