package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.LocationResponseDTO;
import br.com.geac.backend.aplication.dtos.request.LocationPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.LocationRequestDTO;
import br.com.geac.backend.aplication.mappers.LocationMapper;
import br.com.geac.backend.domain.entities.Location;
import br.com.geac.backend.domain.exceptions.LocationAlreadyExistsException;
import br.com.geac.backend.domain.exceptions.LocationNotFoundException;
import br.com.geac.backend.infrastucture.repositories.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Transactional
    public LocationResponseDTO createLocation(LocationRequestDTO dto) {
        if (locationRepository.existsByZipCodeAndNumberAndName(dto.zipCode(), dto.number(), dto.name())) {
            throw new LocationAlreadyExistsException("Location with the same zip code,number and name already exists"); // se precisar poe reference point tbm
        }
        var location = locationMapper.toEntity(dto);
        return locationMapper.toDto(locationRepository.save(location));
    }

    public LocationResponseDTO getById(Integer id) {
        var location = getLocationByIdOrThrownBadRequest(id);
        return locationMapper.toDto(location);
    }

    public List<LocationResponseDTO> getAll() {
        return locationRepository.findAll().stream()
                .map(locationMapper::toDto)
                .toList();
    }

    @Transactional
    public LocationResponseDTO updateLocation(Integer id, LocationPatchRequestDTO dto) {

        var location = getLocationByIdOrThrownBadRequest(id);

        String newZipCode = dto.zipCode() != null ? dto.zipCode() : location.getZipCode();
        String newNumber = dto.number() != null ? dto.number() : location.getNumber();
        String newName = dto.name() != null ? dto.name() : location.getName();

        if (locationRepository.existsByZipCodeAndNumberAndNameAndIdNot(newZipCode, newNumber, newName, id)) { // Se precisar colocar referencepoint tbm
            throw new LocationAlreadyExistsException("Another location with the same zip code,number and name already exists");
        }

        locationMapper.updateEntityFromDTO(dto, location);
        return locationMapper.toDto(locationRepository.save(location));
    }

    @Transactional
    public void deleteLocation(Integer id) {

        var location = getLocationByIdOrThrownBadRequest(id);
        locationRepository.delete(location);
        locationRepository.flush(); //pode dar erro de constraint

    }

    private Location getLocationByIdOrThrownBadRequest(Integer id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
    }
}
