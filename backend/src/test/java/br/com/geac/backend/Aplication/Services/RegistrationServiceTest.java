package br.com.geac.backend.Aplication.Services;

import br.com.geac.backend.Aplication.DTOs.Reponse.RegistrationResponseDTO;
import br.com.geac.backend.Domain.Entities.*;
import br.com.geac.backend.Domain.Enums.EventStatus;
import br.com.geac.backend.Domain.Enums.Role;
import br.com.geac.backend.Domain.Exceptions.*;
import br.com.geac.backend.Infrastructure.Repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private NotificationService notificationService;
    @Mock private OrganizerMemberRepository organizerMemberRepository;
    @Mock private CertificateService certificateService;

    @InjectMocks
    private RegistrationService registrationService;

    private User student;
    private User admin;
    private Event event;
    private Organizer organizer;
    private Registration registration;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setEmail("aluno@ufape.br");
        student.setName("Aluno Teste");
        student.setRole(Role.STUDENT);

        admin = new User();
        admin.setEmail("admin@ufape.br");
        admin.setName("Admin Teste");
        admin.setRole(Role.ADMIN);

        organizer = new Organizer();
        organizer.setId(UUID.randomUUID());
        organizer.setName("Org Teste");

        event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("Palestra sobre IA");
        event.setStatus(EventStatus.ACTIVE);
        event.setOrganizer(organizer);
        event.setMaxCapacity(100);
        event.setSpeakers(new HashSet<>());
        event.setTags(new HashSet<>());
        event.setRequirements(new HashSet<>());

        registration = new Registration();
        registration.setUser(student);
        registration.setEvent(event);
        registration.setStatus("CONFIRMED");
        registration.setAttended(false);
    }

    // ==================== REGISTER TO EVENT ====================

    @Test
    @DisplayName("Deve inscrever usuário no evento com sucesso")
    void registerToEvent_Success() {
        setAuthentication(student);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(10L);
        when(eventRepository.save(any())).thenReturn(event);
        when(registrationRepository.save(any())).thenReturn(registration);

        assertThatCode(() -> registrationService.registerToEvent(event.getId()))
                .doesNotThrowAnyException();

        verify(registrationRepository).save(any(Registration.class));
        verify(notificationService).notify(any(Notification.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando evento não encontrado ao inscrever")
    void registerToEvent_EventNotFound_ThrowsException() {
        setAuthentication(student);

        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.registerToEvent(UUID.randomUUID()))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário já está inscrito")
    void registerToEvent_AlreadySubscribed_ThrowsException() {
        setAuthentication(student);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerToEvent(event.getId()))
                .isInstanceOf(UserAlreadySubscribedInEvent.class)
                .hasMessage("Você já está inscrito neste evento.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário é membro da organização promotora")
    void registerToEvent_MemberOfPromoterOrg_ThrowsException() {
        setAuthentication(student);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerToEvent(event.getId()))
                .isInstanceOf(MemberOfPromoterOrgException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando evento está lotado")
    void registerToEvent_MaxCapacity_ThrowsException() {
        setAuthentication(student);
        event.setMaxCapacity(10);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(10L);

        assertThatThrownBy(() -> registrationService.registerToEvent(event.getId()))
                .isInstanceOf(EventMaxCapacityAchievedException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando evento não está disponível")
    void registerToEvent_EventNotAvailable_ThrowsException() {
        setAuthentication(student);
        event.setStatus(EventStatus.COMPLETED);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(5L);

        assertThatThrownBy(() -> registrationService.registerToEvent(event.getId()))
                .isInstanceOf(EventNotAvailableException.class);
    }

    @Test
    @DisplayName("Deve inscrever com sucesso em evento UPCOMING")
    void registerToEvent_UpcomingEvent_Success() {
        setAuthentication(student);
        event.setStatus(EventStatus.UPCOMING);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(5L);
        when(eventRepository.save(any())).thenReturn(event);
        when(registrationRepository.save(any())).thenReturn(registration);

        assertThatCode(() -> registrationService.registerToEvent(event.getId()))
                .doesNotThrowAnyException();
    }

    // ==================== CANCEL REGISTRATION ====================

    @Test
    @DisplayName("Deve cancelar inscrição com sucesso")
    void cancelRegistration_Success() {
        setAuthentication(student);

        when(registrationRepository.findByUserIdAndEventId(any(), any()))
                .thenReturn(Optional.of(registration));

        assertThatCode(() -> registrationService.cancelRegistration(event.getId()))
                .doesNotThrowAnyException();

        verify(registrationRepository).delete(registration);
        verify(notificationService).notify(any(Notification.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando inscrição não encontrada ao cancelar")
    void cancelRegistration_NotFound_ThrowsException() {
        setAuthentication(student);

        when(registrationRepository.findByUserIdAndEventId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.cancelRegistration(event.getId()))
                .isInstanceOf(RegistrationNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando presença já foi validada")
    void cancelRegistration_AlreadyAttended_ThrowsException() {
        setAuthentication(student);
        registration.setAttended(true);

        when(registrationRepository.findByUserIdAndEventId(any(), any()))
                .thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> registrationService.cancelRegistration(event.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("presença já foi validada");
    }

    // ==================== GET REGISTRATIONS BY EVENT ====================

    @Test
    @DisplayName("Deve retornar lista de inscrições por evento")
    void getRegistrationsByEvent_Success() {
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventId(event.getId())).thenReturn(List.of(registration));

        List<RegistrationResponseDTO> result = registrationService.getRegistrationsByEvent(event.getId());

        assertThat(result).isNotNull().hasSize(1);
    }

    @Test
    @DisplayName("Deve lançar exceção quando evento não encontrado ao buscar inscrições")
    void getRegistrationsByEvent_EventNotFound_ThrowsException() {
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.getRegistrationsByEvent(UUID.randomUUID()))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há inscrições")
    void getRegistrationsByEvent_EmptyList() {
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventId(event.getId())).thenReturn(List.of());

        List<RegistrationResponseDTO> result = registrationService.getRegistrationsByEvent(event.getId());

        assertThat(result).isNotNull().isEmpty();
    }

    // ==================== MARK ATTENDANCE ====================

    @Test
    @DisplayName("Deve marcar presença em bulk com sucesso como admin")
    void markAttendanceInBulk_AdminSuccess() {
        setAuthentication(admin);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);

        List<UUID> userIds = List.of(UUID.randomUUID());

        assertThatCode(() -> registrationService.markAttendanceInBulk(event.getId(), userIds, true))
                .doesNotThrowAnyException();

        verify(registrationRepository).updateAttendanceInBulk(event.getId(), userIds, true);
        verify(certificateService).issueCertificatesForEvent(event.getId());
    }

    @Test
    @DisplayName("Não deve emitir certificados quando attended é false")
    void markAttendanceInBulk_NotAttended_NoCertificates() {
        setAuthentication(admin);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);

        List<UUID> userIds = List.of(UUID.randomUUID());

        registrationService.markAttendanceInBulk(event.getId(), userIds, false);

        verify(certificateService, never()).issueCertificatesForEvent(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando evento não encontrado ao marcar presença")
    void markAttendanceInBulk_EventNotFound_ThrowsException() {
        setAuthentication(admin);

        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.markAttendanceInBulk(UUID.randomUUID(), List.of(), true))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é membro da organização")
    void markAttendanceInBulk_NotMember_ThrowsException() {
        setAuthentication(student);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> registrationService.markAttendanceInBulk(event.getId(), List.of(), true))
                .isInstanceOf(BadRequestException.class);
    }

    // ==================== SAVE ALL ====================

    @Test
    @DisplayName("Deve salvar lista de inscrições")
    void saveAll_Success() {
        List<Registration> registrations = List.of(registration);
        registrationService.saveAll(registrations);
        verify(registrationRepository).saveAll(registrations);
    }

    // ==================== GET UNNOTIFIED ====================

    @Test
    @DisplayName("Deve retornar inscrições não notificadas")
    void getUnotifiedRegistrationsById_Success() {
        when(registrationRepository.findByEventIdAndNotified(event.getId(), false))
                .thenReturn(List.of(registration));

        List<Registration> result = registrationService.getUnotifiedRegistrationsById(event.getId());

        assertThat(result).hasSize(1);
    }

    // ==================== HELPERS ====================

    private void setAuthentication(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}