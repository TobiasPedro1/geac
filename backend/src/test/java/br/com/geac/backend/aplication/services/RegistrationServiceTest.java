package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.RegistrationResponseDTO;
import br.com.geac.backend.domain.entities.*;
import br.com.geac.backend.domain.enums.EventStatus;
import br.com.geac.backend.domain.enums.Role;
import br.com.geac.backend.domain.exceptions.*;
import br.com.geac.backend.infrastucture.repositories.*;
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
    @DisplayName("Deve inscrever usuario no evento com sucesso")
    void registerToEvent_Success() {
        setAuthentication(student);
        UUID eventId = event.getId();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(10L);
        when(eventRepository.save(any())).thenReturn(event);
        when(registrationRepository.save(any())).thenReturn(registration);

        assertThatCode(() -> registrationService.registerToEvent(eventId))
                .doesNotThrowAnyException();

        verify(registrationRepository).save(any(Registration.class));
        verify(notificationService).notify(any(Notification.class));
    }

    @Test
    @DisplayName("Deve lancar excecao quando evento nao encontrado ao inscrever")
    void registerToEvent_EventNotFound_ThrowsException() {
        setAuthentication(student);
        UUID missingEventId = UUID.randomUUID();

        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.registerToEvent(missingEventId))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lancar excecao quando usuario ja esta inscrito")
    void registerToEvent_AlreadySubscribed_ThrowsException() {
        setAuthentication(student);
        UUID eventId = event.getId();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerToEvent(eventId))
                .isInstanceOf(UserAlreadySubscribedInEvent.class)
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve lancar excecao quando usuario e membro da organizacao promotora")
    void registerToEvent_MemberOfPromoterOrg_ThrowsException() {
        setAuthentication(student);
        UUID eventId = event.getId();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerToEvent(eventId))
                .isInstanceOf(MemberOfPromoterOrgException.class);
    }

    @Test
    @DisplayName("Deve lancar excecao quando evento esta lotado")
    void registerToEvent_MaxCapacity_ThrowsException() {
        setAuthentication(student);
        UUID eventId = event.getId();
        event.setMaxCapacity(10);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(10L);

        assertThatThrownBy(() -> registrationService.registerToEvent(eventId))
                .isInstanceOf(EventMaxCapacityAchievedException.class);
    }

    @Test
    @DisplayName("Deve lancar excecao quando evento nao esta disponivel")
    void registerToEvent_EventNotAvailable_ThrowsException() {
        setAuthentication(student);
        UUID eventId = event.getId();
        event.setStatus(EventStatus.COMPLETED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(5L);

        assertThatThrownBy(() -> registrationService.registerToEvent(eventId))
                .isInstanceOf(EventNotAvailableException.class);
    }

    @Test
    @DisplayName("Deve inscrever com sucesso em evento UPCOMING")
    void registerToEvent_UpcomingEvent_Success() {
        setAuthentication(student);
        UUID eventId = event.getId();
        event.setStatus(EventStatus.UPCOMING);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventId(any(), any())).thenReturn(false);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(5L);
        when(eventRepository.save(any())).thenReturn(event);
        when(registrationRepository.save(any())).thenReturn(registration);

        assertThatCode(() -> registrationService.registerToEvent(eventId))
                .doesNotThrowAnyException();
    }

    // ==================== CANCEL REGISTRATION ====================

    @Test
    @DisplayName("Deve cancelar inscricao com sucesso")
    void cancelRegistration_Success() {
        setAuthentication(student);
        UUID eventId = event.getId();

        when(registrationRepository.findByUserIdAndEventId(any(), any()))
                .thenReturn(Optional.of(registration));

        assertThatCode(() -> registrationService.cancelRegistration(eventId))
                .doesNotThrowAnyException();

        verify(registrationRepository).delete(registration);
        verify(notificationService).notify(any(Notification.class));
    }

    @Test
    @DisplayName("Deve lancar excecao quando inscricao nao encontrada ao cancelar")
    void cancelRegistration_NotFound_ThrowsException() {
        setAuthentication(student);
        UUID eventId = event.getId();

        when(registrationRepository.findByUserIdAndEventId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.cancelRegistration(eventId))
                .isInstanceOf(RegistrationNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lancar excecao quando presenca ja foi validada")
    void cancelRegistration_AlreadyAttended_ThrowsException() {
        setAuthentication(student);
        UUID eventId = event.getId();
        registration.setAttended(true);

        when(registrationRepository.findByUserIdAndEventId(any(), any()))
                .thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> registrationService.cancelRegistration(eventId))
                .isInstanceOf(BadRequestException.class)
                .isInstanceOf(Exception.class);
    }

    // ==================== GET REGISTRATIONS BY EVENT ====================

    @Test
    @DisplayName("Deve retornar lista de inscricoes por evento")
    void getRegistrationsByEvent_Success() {
        setAuthentication(admin);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(true);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventId(event.getId())).thenReturn(List.of(registration));

        List<RegistrationResponseDTO> result = registrationService.getRegistrationsByEvent(event.getId());

        assertThat(result).isNotNull().hasSize(1);
    }

    @Test
    @DisplayName("Deve lancar excecao quando evento nao encontrado ao buscar inscricoes")
    void getRegistrationsByEvent_EventNotFound_ThrowsException() {
        UUID missingEventId = UUID.randomUUID();
        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.getRegistrationsByEvent(missingEventId))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao ha inscricoes")
    void getRegistrationsByEvent_EmptyList() {
        setAuthentication(admin);
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(true);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventId(event.getId())).thenReturn(List.of());

        List<RegistrationResponseDTO> result = registrationService.getRegistrationsByEvent(event.getId());

        assertThat(result).isNotNull().isEmpty();
    }

    // ==================== MARK ATTENDANCE ====================

    @Test
    @DisplayName("Deve marcar presenca em bulk com sucesso como admin")
    void markAttendanceInBulk_AdminSuccess() {
        setAuthentication(admin);
        UUID eventId = event.getId();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);

        List<UUID> userIds = List.of(UUID.randomUUID());

        assertThatCode(() -> registrationService.markAttendanceInBulk(eventId, userIds, true))
                .doesNotThrowAnyException();

        verify(registrationRepository).updateAttendanceInBulk(eventId, userIds, true);
        verify(certificateService).issueCertificatesForEvent(eventId);
    }

    @Test
    @DisplayName("Nao deve emitir certificados quando attended e false")
    void markAttendanceInBulk_NotAttended_NoCertificates() {
        setAuthentication(admin);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);

        List<UUID> userIds = List.of(UUID.randomUUID());

        registrationService.markAttendanceInBulk(event.getId(), userIds, false);

        verify(certificateService, never()).issueCertificatesForEvent(any());
    }

    @Test
    @DisplayName("Deve lancar excecao quando evento nao encontrado ao marcar presenca")
    void markAttendanceInBulk_EventNotFound_ThrowsException() {
        setAuthentication(admin);
        UUID missingEventId = UUID.randomUUID();
        List<UUID> userIds = List.of();

        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.markAttendanceInBulk(missingEventId, userIds, true))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lancar excecao quando usuario nao e membro da organizacao")
    void markAttendanceInBulk_NotMember_ThrowsException() {
        setAuthentication(student);
        UUID eventId = event.getId();
        List<UUID> userIds = List.of();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> registrationService.markAttendanceInBulk(eventId, userIds, true))
                .isInstanceOf(BadRequestException.class);
    }

    // ==================== SAVE ALL ====================

    @Test
    @DisplayName("Deve salvar lista de inscricoes")
    void saveAll_Success() {
        List<Registration> registrations = List.of(registration);
        registrationService.saveAll(registrations);
        verify(registrationRepository).saveAll(registrations);
    }

    // ==================== GET UNNOTIFIED ====================

    @Test
    @DisplayName("Deve retornar inscricoes nao notificadas")
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

