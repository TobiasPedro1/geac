package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.request.CreateOrganizerRequestDTO;
import br.com.geac.backend.aplication.dtos.response.PendingRequestResponseDTO;
import br.com.geac.backend.domain.entities.Notification;
import br.com.geac.backend.domain.entities.Organizer;
import br.com.geac.backend.domain.entities.OrganizerRequest;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.RequestStatus;
import br.com.geac.backend.domain.enums.Role;
import br.com.geac.backend.domain.exceptions.ConflictException;
import br.com.geac.backend.domain.exceptions.OrganizerNotFoundExceptio;
import br.com.geac.backend.domain.exceptions.RequestAlreadyExists;
import br.com.geac.backend.domain.exceptions.UserIsAlreadyOrgMember;
import br.com.geac.backend.domain.exceptions.UserNotFoundException;
import br.com.geac.backend.infrastucture.repositories.OrganizerMemberRepository;
import br.com.geac.backend.infrastucture.repositories.OrganizerRepository;
import br.com.geac.backend.infrastucture.repositories.OrganizerRequestRepository;
import br.com.geac.backend.infrastucture.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizerRequestServiceTest {

    @Mock private OrganizerRequestRepository requestRepository;
    @Mock private OrganizerMemberRepository memberRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrganizerRepository organizerRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private OrganizerRequestService service;

    private User user;
    private User admin;
    private Organizer organizer;
    private OrganizerRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("User Name");
        user.setEmail("user@test.com");
        user.setRole(Role.STUDENT);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setName("Admin Name");
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);

        organizer = new Organizer();
        organizer.setId(UUID.randomUUID());
        organizer.setName("Org");
        organizer.setContactEmail("org@test.com");

        request = new OrganizerRequest();
        request.setId(10);
        request.setUser(user);
        request.setOrganizer(organizer);
        request.setStatus(RequestStatus.PENDING);
        request.setJustification("I can help");
        request.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getPendingRequests deve mapear solicitacoes pendentes")
    void getPendingRequests_Success() {
        when(requestRepository.findAllByStatus(RequestStatus.PENDING)).thenReturn(List.of(request));

        List<PendingRequestResponseDTO> result = service.getPendingRequests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userName()).isEqualTo("User Name");
        assertThat(result.get(0).organizerName()).isEqualTo("Org");
    }

    @Test
    @DisplayName("approveRequest deve lancar excecao quando solicitacao ja foi resolvida")
    void approveRequest_ConflictWhenNotPending() {
        request.setStatus(RequestStatus.REJECTED);
        when(requestRepository.findById(10)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.approveRequest(10))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("approveRequest deve lancar excecao quando usuario nao existe")
    void approveRequest_UserNotFound() {
        when(requestRepository.findById(10)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveRequest(10))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("approveRequest deve salvar membro, promover estudante e notificar")
    void approveRequest_PromotesAndNotifies() {
        when(requestRepository.findById(10)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(memberRepository.existsByOrganizerIdAndUserId(organizer.getId(), user.getId())).thenReturn(false);

        assertThatCode(() -> service.approveRequest(10)).doesNotThrowAnyException();

        verify(requestRepository).save(any(OrganizerRequest.class));
        verify(memberRepository).save(any());
        verify(userRepository).save(user);
        verify(notificationService).notify(any(Notification.class));
        assertThat(user.getRole()).isEqualTo(Role.ORGANIZER);
    }

    @Test
    @DisplayName("approveRequest nao deve criar membro em duplicidade")
    void approveRequest_AlreadyMember() {
        user.setRole(Role.ADMIN);
        when(requestRepository.findById(10)).thenReturn(Optional.of(request));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(memberRepository.existsByOrganizerIdAndUserId(organizer.getId(), user.getId())).thenReturn(true);

        service.approveRequest(10);

        verify(memberRepository, never()).save(any());
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).notify(any(Notification.class));
    }

    @Test
    @DisplayName("rejectRequest deve marcar como rejeitada e notificar usuario")
    void rejectRequest_Success() {
        when(requestRepository.findById(10)).thenReturn(Optional.of(request));

        service.rejectRequest(10);

        verify(requestRepository).save(any(OrganizerRequest.class));
        verify(notificationService).notify(any(Notification.class));
        assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(request.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("createRequest deve lancar excecao quando existe solicitacao pendente")
    void createRequest_PendingExists() {
        CreateOrganizerRequestDTO dto = new CreateOrganizerRequestDTO(user.getId(), organizer.getId(), "text");
        when(requestRepository.existsByUserIdAndOrganizerIdAndStatus(user.getId(), organizer.getId(), RequestStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createRequest(dto))
                .isInstanceOf(RequestAlreadyExists.class);
    }

    @Test
    @DisplayName("createRequest deve lancar excecao quando usuario ja e membro")
    void createRequest_AlreadyMember() {
        CreateOrganizerRequestDTO dto = new CreateOrganizerRequestDTO(user.getId(), organizer.getId(), "text");
        when(requestRepository.existsByUserIdAndOrganizerIdAndStatus(user.getId(), organizer.getId(), RequestStatus.PENDING))
                .thenReturn(false);
        when(memberRepository.existsByOrganizerIdAndUserId(organizer.getId(), user.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.createRequest(dto))
                .isInstanceOf(UserIsAlreadyOrgMember.class);
    }

    @Test
    @DisplayName("createRequest deve lancar excecao quando organizador nao existe")
    void createRequest_OrganizerNotFound() {
        CreateOrganizerRequestDTO dto = new CreateOrganizerRequestDTO(user.getId(), organizer.getId(), "text");
        when(requestRepository.existsByUserIdAndOrganizerIdAndStatus(user.getId(), organizer.getId(), RequestStatus.PENDING))
                .thenReturn(false);
        when(memberRepository.existsByOrganizerIdAndUserId(organizer.getId(), user.getId())).thenReturn(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(organizerRepository.findById(organizer.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createRequest(dto))
                .isInstanceOf(OrganizerNotFoundExceptio.class);
    }

    @Test
    @DisplayName("createRequest deve persistir solicitacao e notificar admins")
    void createRequest_Success() {
        CreateOrganizerRequestDTO dto = new CreateOrganizerRequestDTO(user.getId(), organizer.getId(), "join me");
        when(requestRepository.existsByUserIdAndOrganizerIdAndStatus(user.getId(), organizer.getId(), RequestStatus.PENDING))
                .thenReturn(false);
        when(memberRepository.existsByOrganizerIdAndUserId(organizer.getId(), user.getId())).thenReturn(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(organizerRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(List.of(admin, admin));

        service.createRequest(dto);

        ArgumentCaptor<OrganizerRequest> requestCaptor = ArgumentCaptor.forClass(OrganizerRequest.class);
        verify(requestRepository).save(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(requestCaptor.getValue().getJustification()).isEqualTo("join me");
        verify(notificationService, times(2)).notify(any(Notification.class));
    }
}


