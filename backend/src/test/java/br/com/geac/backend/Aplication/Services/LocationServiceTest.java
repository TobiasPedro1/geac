package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.LocationResponseDTO;
import br.com.geac.backend.aplication.dtos.request.LocationPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.LocationRequestDTO;
import br.com.geac.backend.aplication.mappers.LocationMapper;
import br.com.geac.backend.domain.entities.Location;
import br.com.geac.backend.domain.enums.Campus;
import br.com.geac.backend.domain.exceptions.LocationAlreadyExistsException;
import br.com.geac.backend.domain.exceptions.LocationNotFoundException;
import br.com.geac.backend.infrastucture.repositories.LocationRepository;
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
class LocationServiceTest {

    @Mock private LocationRepository locationRepository;
    @Mock private LocationMapper locationMapper;

    @InjectMocks
    private LocationService locationService;

    private Location location;
    private LocationResponseDTO locationResponse;
    private LocationRequestDTO locationRequest;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setId(1);
        location.setName("AuditÃ³rio UFAPE");
        location.setZipCode("55292-270");
        location.setNumber("S/N");

        locationResponse = new LocationResponseDTO(
                1, "AuditÃ³rio UFAPE", "Rua Principal", "S/N",
                "Centro", "Garanhuns", "PE", "55292-270",
                Campus.CAMPUS_CARUARU_CENTRAL, null, 200
        );

        locationRequest = new LocationRequestDTO(
                "AuditÃ³rio UFAPE", "Rua Principal", "S/N",
                "Centro", "Garanhuns", "PE", "55292-270",
                null, 200, Campus.CAMPUS_CARUARU_CENTRAL
        );
    }

    @Test
    @DisplayName("Deve criar localizacao com sucesso")
    void createLocation_Success() {
        when(locationRepository.existsByZipCodeAndNumberAndName(any(), any(), any())).thenReturn(false);
        when(locationMapper.toEntity(locationRequest)).thenReturn(location);
        when(locationRepository.save(location)).thenReturn(location);
        when(locationMapper.toDto(location)).thenReturn(locationResponse);

        LocationResponseDTO result = locationService.createLocation(locationRequest);

        assertThat(result).isNotNull();
        verify(locationRepository).save(location);
    }

    @Test
    @DisplayName("Deve lancar excecao quando localizacao ja existe")
    void createLocation_AlreadyExists_ThrowsException() {
        when(locationRepository.existsByZipCodeAndNumberAndName(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> locationService.createLocation(locationRequest))
                .isInstanceOf(LocationAlreadyExistsException.class);

        verify(locationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar localizacao por ID")
    void getById_Success() {
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(locationResponse);

        LocationResponseDTO result = locationService.getById(1);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Deve lancar excecao quando localizacao nao encontrada")
    void getById_NotFound_ThrowsException() {
        when(locationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getById(99))
                .isInstanceOf(LocationNotFoundException.class);
    }

    @Test
    @DisplayName("Deve retornar todas as localizacoes")
    void getAll_Success() {
        when(locationRepository.findAll()).thenReturn(List.of(location));
        when(locationMapper.toDto(location)).thenReturn(locationResponse);

        List<LocationResponseDTO> result = locationService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao ha localizacoes")
    void getAll_EmptyList() {
        when(locationRepository.findAll()).thenReturn(List.of());

        List<LocationResponseDTO> result = locationService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar localizacao com sucesso")
    void updateLocation_Success() {
        LocationPatchRequestDTO patchDTO = new LocationPatchRequestDTO(
                "Novo Nome", null, null, null, null, null, null, null, null, null
        );

        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        when(locationRepository.existsByZipCodeAndNumberAndNameAndIdNot(any(), any(), any(), any())).thenReturn(false);
        when(locationRepository.save(location)).thenReturn(location);
        when(locationMapper.toDto(location)).thenReturn(locationResponse);

        LocationResponseDTO result = locationService.updateLocation(1, patchDTO);

        assertThat(result).isNotNull();
        verify(locationRepository).save(location);
    }

    @Test
    @DisplayName("Deve lancar excecao ao atualizar com nome duplicado")
    void updateLocation_DuplicateName_ThrowsException() {
        LocationPatchRequestDTO patchDTO = new LocationPatchRequestDTO(
                "Outro Local", null, null, null, null, null, null, null, null, null
        );

        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        when(locationRepository.existsByZipCodeAndNumberAndNameAndIdNot(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> locationService.updateLocation(1, patchDTO))
                .isInstanceOf(LocationAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Deve lancar excecao ao atualizar localizacao inexistente")
    void updateLocation_NotFound_ThrowsException() {
        when(locationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.updateLocation(99,
                new LocationPatchRequestDTO(null, null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(LocationNotFoundException.class);
    }

    @Test
    @DisplayName("Deve deletar localizacao com sucesso")
    void deleteLocation_Success() {
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));

        assertThatCode(() -> locationService.deleteLocation(1)).doesNotThrowAnyException();

        verify(locationRepository).delete(location);
        verify(locationRepository).flush();
    }

    @Test
    @DisplayName("Deve lancar excecao ao deletar localizacao inexistente")
    void deleteLocation_NotFound_ThrowsException() {
        when(locationRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.deleteLocation(99))
                .isInstanceOf(LocationNotFoundException.class);
    }
}

