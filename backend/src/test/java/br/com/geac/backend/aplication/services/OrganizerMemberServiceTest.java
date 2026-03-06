package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.request.AddMemberRequestDTO;
import br.com.geac.backend.aplication.dtos.response.MemberResponseDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerMemberServiceTest {

    @Mock private OrganizerMemberRepository memberRepository;
    @Mock private OrganizerRepository organizerRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private OrganizerMemberService service;

    private UUID organizerId;
    private UUID userId;
    private Organizer organizer;
    private User user;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        userId = UUID.randomUUID();

        organizer = new Organizer();
        organizer.setId(organizerId);
        organizer.setName("Org");

        user = new User();
        user.setId(userId);
        user.setName("User");
        user.setEmail("user@test.com");
        user.setRole(Role.STUDENT);
    }

    @Test
    @DisplayName("addMember deve lancar excecao quando organizador nao existe")
    void addMember_OrganizerNotFound() {
        AddMemberRequestDTO request = new AddMemberRequestDTO(userId);
        when(organizerRepository.findById(organizerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addMember(organizerId, request))
                .isInstanceOf(OrganizerNotFoundExceptio.class);
    }

    @Test
    @DisplayName("addMember deve lancar excecao quando usuario nao existe")
    void addMember_UserNotFound() {
        AddMemberRequestDTO request = new AddMemberRequestDTO(userId);
        when(organizerRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addMember(organizerId, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("addMember deve lancar excecao quando usuario ja e membro")
    void addMember_AlreadyMember() {
        AddMemberRequestDTO request = new AddMemberRequestDTO(userId);
        when(organizerRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(memberRepository.existsByOrganizerIdAndUserId(organizerId, userId)).thenReturn(true);

        assertThatThrownBy(() -> service.addMember(organizerId, request))
                .isInstanceOf(UserIsAlreadyOrgMember.class);
    }

    @Test
    @DisplayName("addMember deve promover estudante para organizador")
    void addMember_PromoteStudent() {
        AddMemberRequestDTO request = new AddMemberRequestDTO(userId);
        when(organizerRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(memberRepository.existsByOrganizerIdAndUserId(organizerId, userId)).thenReturn(false);

        assertThatCode(() -> service.addMember(organizerId, request))
                .doesNotThrowAnyException();

        verify(userRepository).save(user);
        verify(memberRepository).save(any(OrganizerMember.class));
        assertThat(user.getRole()).isEqualTo(Role.ORGANIZER);
    }

    @Test
    @DisplayName("addMember nao deve alterar papel para admin")
    void addMember_KeepAdminRole() {
        user.setRole(Role.ADMIN);
        when(organizerRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(memberRepository.existsByOrganizerIdAndUserId(organizerId, userId)).thenReturn(false);

        service.addMember(organizerId, new AddMemberRequestDTO(userId));

        verify(userRepository, never()).save(user);
        verify(memberRepository).save(any(OrganizerMember.class));
    }

    @Test
    @DisplayName("getMembersByOrganizerId deve lancar excecao quando organizador nao existe")
    void getMembersByOrganizerId_OrganizerNotFound() {
        when(organizerRepository.existsById(organizerId)).thenReturn(false);

        assertThatThrownBy(() -> service.getMembersByOrganizerId(organizerId))
                .isInstanceOf(OrganizerNotFoundExceptio.class);
    }

    @Test
    @DisplayName("getMembersByOrganizerId deve mapear dados do repositorio")
    void getMembersByOrganizerId_Success() {
        OrganizerMember member = new OrganizerMember();
        member.setOrganizer(organizer);
        member.setUser(user);
        member.setCreatedAt(LocalDateTime.now());

        when(organizerRepository.existsById(organizerId)).thenReturn(true);
        when(memberRepository.findAllByOrganizerId(organizerId)).thenReturn(List.of(member));

        List<MemberResponseDTO> result = service.getMembersByOrganizerId(organizerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("User");
    }

    @Test
    @DisplayName("removeMember deve lancar excecao quando vinculo nao existe")
    void removeMember_LinkNotFound() {
        when(memberRepository.findByOrganizerIdAndUserId(organizerId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeMember(organizerId, userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("removeMember deve lancar excecao quando usuario nao encontrado")
    void removeMember_UserNotFound() {
        OrganizerMember member = new OrganizerMember();
        when(memberRepository.findByOrganizerIdAndUserId(organizerId, userId)).thenReturn(Optional.of(member));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeMember(organizerId, userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("removeMember deve rebaixar organizador para estudante")
    void removeMember_DemoteOrganizer() {
        OrganizerMember member = new OrganizerMember();
        user.setRole(Role.ORGANIZER);
        when(memberRepository.findByOrganizerIdAndUserId(organizerId, userId)).thenReturn(Optional.of(member));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.removeMember(organizerId, userId);

        assertThat(user.getRole()).isEqualTo(Role.STUDENT);
        verify(userRepository).save(user);
        verify(memberRepository).delete(member);
    }
}


