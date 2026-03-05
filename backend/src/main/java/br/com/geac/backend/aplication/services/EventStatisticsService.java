package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.EventStatisticsResponseDTO;
import br.com.geac.backend.aplication.mappers.EventStatisticsMapper;
import br.com.geac.backend.infrastucture.repositories.EventStatisticsRepositoryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventStatisticsService {

    private final EventStatisticsRepositoryView repository;
    private final EventStatisticsMapper mapper;

    public List<EventStatisticsResponseDTO> getAllEventStatistics() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }
}
