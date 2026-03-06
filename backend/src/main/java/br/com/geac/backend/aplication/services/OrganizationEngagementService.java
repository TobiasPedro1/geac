package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.OrganizationEngagementResponseDTO;
import br.com.geac.backend.aplication.mappers.OrganizationEngagementMapper;
import br.com.geac.backend.infrastucture.repositories.OrganizationEngagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationEngagementService {

    private final OrganizationEngagementRepository repository;
    private final OrganizationEngagementMapper mapper;

    public List<OrganizationEngagementResponseDTO> getAllOrganizationEngagement() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }
}
