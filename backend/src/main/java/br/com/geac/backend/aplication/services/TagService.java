package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.TagResponseDTO;
import br.com.geac.backend.aplication.dtos.request.TagRequestDTO;
import br.com.geac.backend.aplication.mappers.TagMapper;
import br.com.geac.backend.domain.entities.Tag;
import br.com.geac.backend.domain.exceptions.TagNotFoundException;
import br.com.geac.backend.infrastucture.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper mapper;

    @Transactional
    public TagResponseDTO createTag(TagRequestDTO dto) {

        var saving = mapper.toEntity(dto);
        return mapper.toDTO(tagRepository.save(saving)); // se der erro é a constraint do banco
    }

    public TagResponseDTO getById(Integer id) {
        var tag = getTagOrThrowBadRequest(id);
        return mapper.toDTO(tag);
    }

    public List<TagResponseDTO> getAll() {
        return tagRepository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    public TagResponseDTO updateSpeaker(Integer id, TagRequestDTO dto) {
        var tag = getTagOrThrowBadRequest(id);
        tag.setName(dto.name());
        return mapper.toDTO(tagRepository.save(tag));
    }

    public void deleteTag(Integer id) {
        var tag = getTagOrThrowBadRequest(id);
        tagRepository.delete(tag);
    }

    private Tag getTagOrThrowBadRequest(Integer id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Tag not found"));
    }

}
