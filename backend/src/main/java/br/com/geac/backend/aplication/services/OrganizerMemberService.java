package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.MemberResponseDTO;
import br.com.geac.backend.aplication.dtos.request.AddMemberRequestDTO;
import br.com.geac.backend.domain.entities.Organizer;
import br.com.geac.backend.domain.entities.OrganizerMember;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.Role;
import br.com.geac.backend.domain.exceptions.OrganizerNotFoundExceptio;
import br.com.geac.backend.domain.exceptions.UserIsAlreadyOrgMember;
import br.com.geac.backend.domain.exceptions.UserNotFoundException;
import br.com.geac.backend.infrastucture.repositories.OrganizerMemberRepository;
import br.com.geac.backend.infrastucture.repositories.OrganizerRepository;
import br.com.geac.backend.infrastucture.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizerMemberService {

    private final OrganizerMemberRepository memberRepository;
    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addMember(UUID organizerId, AddMemberRequestDTO dto) {

        //verifica se a org existe
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new OrganizerNotFoundExceptio("Organização não encontrada."));
        //verifica se o usuario existe
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        //verifica se o usuário ja eh membro
        if (memberRepository.existsByOrganizerIdAndUserId(organizerId, dto.userId())) {
            throw new UserIsAlreadyOrgMember("Este usuário já é membro desta organização.");
        }

        OrganizerMember newMember = new OrganizerMember();
        newMember.setOrganizer(organizer);
        newMember.setUser(user);
        if (user.getRole().equals(Role.STUDENT) || user.getRole().equals(Role.PROFESSOR)) {
            user.setRole(Role.ORGANIZER);
            userRepository.save(user);
        }
        memberRepository.save(newMember);
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDTO> getMembersByOrganizerId(UUID organizerId) {
        if (!organizerRepository.existsById(organizerId)) {
            throw new OrganizerNotFoundExceptio("Organização não encontrada.");
        }

        List<OrganizerMember> members = memberRepository.findAllByOrganizerId(organizerId);

        return members.stream()
                .map(member -> new MemberResponseDTO(
                        member.getUser().getId(),
                        member.getUser().getName(),
                        member.getUser().getEmail(),
                        member.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void removeMember(UUID organizerId, UUID userId) {
        OrganizerMember memberLink = memberRepository.findByOrganizerIdAndUserId(organizerId, userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado nesta organização."));
        var user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User nao existe"));
        if (user.getRole().equals(Role.ORGANIZER)) {
            user.setRole(Role.STUDENT); //todo: aqui teriamos que guardar um old-role seila, mas n acho q precisa por hora
            userRepository.save(user);
        }
        memberRepository.delete(memberLink);
    }
}