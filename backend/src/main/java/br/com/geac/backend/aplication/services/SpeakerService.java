package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.SpeakerResponseDTO;
import br.com.geac.backend.aplication.dtos.request.QualificationRequestDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerPatchRequestDTO;
import br.com.geac.backend.aplication.dtos.request.SpeakerRequestDTO;
import br.com.geac.backend.aplication.mappers.QualificationMapper;
import br.com.geac.backend.aplication.mappers.SpeakerMapper;
import br.com.geac.backend.domain.entities.Qualification;
import br.com.geac.backend.domain.entities.Speaker;
import br.com.geac.backend.domain.exceptions.SpeakerAlreadyExistsException;
import br.com.geac.backend.domain.exceptions.SpeakerNotFoundException;
import br.com.geac.backend.infrastucture.repositories.SpeakerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpeakerService {

    private final SpeakerRepository repository;
    private final SpeakerMapper mapper;
    private final QualificationMapper qualificationMapper;

    @Transactional
    public SpeakerResponseDTO createSpeaker(SpeakerRequestDTO dto) {

        if (repository.existsByNameAndEmail(dto.name(), dto.email())) {
            throw new SpeakerAlreadyExistsException("Speaker with the same name and email already exists");
        }

        var toBeSaved = mapper.toEntity(dto);
        toBeSaved.setQualifications(resolveQualifications(dto.qualifications()));

        return mapper.toDto(repository.save(toBeSaved));
    }

    public SpeakerResponseDTO getById(Integer id) {
        var speaker = getSpeakerOrThrowBadRequest(id);
        return mapper.toDto(speaker);
    }

    public List<SpeakerResponseDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public SpeakerResponseDTO updateSpeaker(Integer id, SpeakerPatchRequestDTO dto) {

        var speaker = getSpeakerOrThrowBadRequest(id);
        mapper.updateEntityFromDTO(dto, speaker);

        if (repository.existsByNameAndEmailAndIdNot(speaker.getName(), speaker.getEmail(), id)) {
            throw new SpeakerAlreadyExistsException("Another speaker with the same name and email already exists");
        }
        //-> NAO MEXER NAO MEXER NAO MEXER <- verificacao meia boca, se der melhorar dps, assim sempre vai recriar no banco
        if (dto.qualifications() != null && !dto.qualifications().isEmpty() && qualificationsChanged(speaker, dto.qualifications())) {
            var qualifications = resolveQualifications(dto.qualifications());
            speaker.setQualifications(qualifications);
        }
        return mapper.toDto(repository.save(speaker));

    }

    @Transactional
    public void deleteSpeaker(Integer id) {
        var speaker = getSpeakerOrThrowBadRequest(id);
        repository.delete(speaker);
    }

    private Speaker getSpeakerOrThrowBadRequest(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new SpeakerNotFoundException("Speaker with id " + id + " not found"));
    }

    private boolean qualificationsChanged(Speaker speaker, Set<QualificationRequestDTO> dtos) {
        if (speaker.getQualifications().size() != dtos.size()) {
            return true;
        }
        return dtos.stream()
                .anyMatch(dto ->
                        speaker.getQualifications()
                                .stream()
                                .noneMatch(q ->
                                        q.getTitleName().equals(dto.titleName())
                                                && q.getInstitution().equals(dto.institution())
                                )
                );
    }

    private Set<Qualification> resolveQualifications(Set<QualificationRequestDTO> dtos) {
        return dtos
                .stream()
                .map(qualificationMapper::toEntity)
                .collect(Collectors.toSet());
    }

}
