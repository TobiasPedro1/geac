package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.EventResponseDTO;
import br.com.geac.backend.aplication.dtos.request.EventRequestDTO;
import br.com.geac.backend.aplication.dtos.request.EventPatchRequestDTO;
import br.com.geac.backend.aplication.mappers.EventMapper;
import br.com.geac.backend.domain.entities.*;
import br.com.geac.backend.domain.enums.DaysBeforeNotify;
import br.com.geac.backend.domain.enums.EventStatus;
import br.com.geac.backend.domain.enums.Role;
import br.com.geac.backend.domain.exceptions.BadRequestException;
import br.com.geac.backend.domain.exceptions.EventAlreadyExistsException;
import br.com.geac.backend.domain.exceptions.EventNotFoundException;
import br.com.geac.backend.infrastucture.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private EventMapper eventMapper;
    @Mock private LocationRepository locationRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private EventRequirementRepository eventRequirementRepository;
    @Mock private TagRepository tagRepository;
    @Mock private SpeakerRepository speakerRepository;
    @Mock private OrganizerMemberRepository organizerMemberRepository;
    @Mock private OrganizerRepository organizerRepository;
    @Mock private RegistrationRepository registrationRepository;

    @InjectMocks
    private EventService eventService;

    private User organizer;
    private User student;
    private Event event;
    private EventResponseDTO eventResponse;
    private Category category;
    private Location location;
    private Organizer org;
    private EventRequestDTO eventRequest;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(UUID.randomUUID());
        organizer.setEmail("org@ufape.br");
        organizer.setName("Organizador Teste");
        organizer.setRole(Role.ORGANIZER);

        student = new User();
        student.setId(UUID.randomUUID());
        student.setEmail("aluno@ufape.br");
        student.setName("Aluno Teste");
        student.setRole(Role.STUDENT);

        category = new Category();
        category.setId(1);
        category.setName("Palestra");

        location = new Location();
        location.setId(1);

        org = new Organizer();
        org.setId(UUID.randomUUID());
        org.setName("Org Teste");

        event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("Palestra sobre IA");
        event.setStartTime(LocalDateTime.now().plusDays(2));
        event.setStatus(EventStatus.ACTIVE);
        event.setOrganizer(org);
        event.setCategory(category);
        event.setSpeakers(new HashSet<>());
        event.setTags(new HashSet<>());
        event.setRequirements(new HashSet<>());

        eventRequest = new EventRequestDTO(
                "Palestra sobre IA",
                "Uma palestra sobre inteligÃªncia artificial",
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                2,
                100,
                1,
                new HashSet<>(),
                new HashSet<>(),
                1,
                new HashSet<>(),
                org.getId(),
                DaysBeforeNotify.ONE_DAY_BEFORE
        );

        eventResponse = EventResponseDTO.builder()
                .id(event.getId())
                .title("Palestra sobre IA")
                .status("ACTIVE")
                .categoryId(1)
                .categoryName("Palestra")
                .organizerName("Org Teste")
                .tags(List.of())
                .speakers(List.of())
                .requirements(List.of())
                .registeredCount(0)
                .build();
    }

    // ==================== CREATE EVENT ====================

    @Test
    @DisplayName("Deve criar evento com sucesso quando usuario e ORGANIZER")
    void createEvent_Success() {
        setAuthentication(organizer);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        when(organizerRepository.findById(org.getId())).thenReturn(Optional.of(org));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(any(), any())).thenReturn(true);
        when(eventRepository.existsByTitleIgnoreCaseAndOrganizerIdAndStartTime(any(), any(), any())).thenReturn(false);
        when(eventMapper.toEntity(any())).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toResponseDTO(any(), any())).thenReturn(eventResponse);

        EventResponseDTO response = eventService.createEvent(eventRequest);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Palestra sobre IA");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Deve lancar AccessDeniedException quando usuario e STUDENT")
    void createEvent_StudentUser_ThrowsAccessDeniedException() {
        setAuthentication(student);

        assertThatThrownBy(() -> eventService.createEvent(eventRequest))
                .isInstanceOf(AccessDeniedException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao quando categoria nao encontrada")
    void createEvent_CategoryNotFound_ThrowsException() {
        setAuthentication(organizer);

        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(eventRequest))
                .isInstanceOf(RuntimeException.class);

        verify(eventRepository, never()).save(any());
    }

    // ==================== GET ALL EVENTS ====================

    @Test
    @DisplayName("Deve retornar lista de eventos com sucesso")
    void getAllEvents_Success() {
        List<Object[]> resultado = new ArrayList<>();
        resultado.add(new Object[]{event, 5L});
        when(eventRepository.findAllWithRegistrationCount()).thenReturn(resultado);
        when(eventMapper.toResponseDTO(any(), any(), any())).thenReturn(eventResponse);

        List<EventResponseDTO> events = eventService.getAllEvents();

        assertThat(events).isNotNull().hasSize(1);
        assertThat(events.get(0).title()).isEqualTo("Palestra sobre IA");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao ha eventos")
    void getAllEvents_EmptyList() {
        when(eventRepository.findAllWithRegistrationCount()).thenReturn(List.of());

        List<EventResponseDTO> events = eventService.getAllEvents();

        assertThat(events).isNotNull().isEmpty();
    }

    // ==================== GET EVENT BY ID ====================

    @Test
    @DisplayName("Deve retornar evento por ID com sucesso")
    void getEventById_Success() {
        UUID id = event.getId();
        setAnonymousAuthentication();

        when(eventRepository.findById(id)).thenReturn(Optional.of(event));
        when(registrationRepository.countByEventIdAndStatus(any(), any())).thenReturn(10L);
        when(eventMapper.toResponseDTO(any(), any(), any())).thenReturn(eventResponse);

        EventResponseDTO response = eventService.getEventById(id);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("Deve lancar excecao quando evento nao encontrado por ID")
    void getEventById_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(id))
                .isInstanceOf(EventNotFoundException.class)
                .isInstanceOf(Exception.class);
    }

    // ==================== DELETE EVENT ====================

    @Test
    @DisplayName("Deve deletar evento com sucesso quando usuario e ADMIN")
    void deleteEvent_AdminUser_Success() {
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(Role.ADMIN);
        setAuthentication(admin);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThatCode(() -> eventService.deleteEvent(event.getId())).doesNotThrowAnyException();
        verify(eventRepository).delete(event);
    }

    @Test
    @DisplayName("Deve lancar excecao quando organizador nao pertence a organizacao do evento ao deletar")
    void deleteEvent_OrganizerNotMember_ThrowsAccessDenied() {
        setAuthentication(organizer);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(org.getId(), organizer.getId())).thenReturn(false);

        assertThatThrownBy(() -> eventService.deleteEvent(event.getId()))
                .isInstanceOf(AccessDeniedException.class);

        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    @DisplayName("Deve lancar excecao quando evento duplicado para mesma organizacao e horario")
    void createEvent_Duplicate_ThrowsEventAlreadyExistsException() {
        setAuthentication(organizer);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        when(organizerRepository.findById(org.getId())).thenReturn(Optional.of(org));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(org.getId(), organizer.getId())).thenReturn(true);
        when(eventRepository.existsByTitleIgnoreCaseAndOrganizerIdAndStartTime(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> eventService.createEvent(eventRequest))
                .isInstanceOf(EventAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Deve lancar excecao quando organizador nao e membro da organizacao informada")
    void createEvent_OrganizerOutOfOrg_ThrowsBadRequestException() {
        setAuthentication(organizer);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        when(organizerRepository.findById(org.getId())).thenReturn(Optional.of(org));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(org.getId(), organizer.getId())).thenReturn(false);

        assertThatThrownBy(() -> eventService.createEvent(eventRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Deve criar evento sem location quando locationId e null")
    void createEvent_WithoutLocation_Success() {
        setAuthentication(organizer);
        EventRequestDTO withoutLocation = new EventRequestDTO(
                eventRequest.title(),
                eventRequest.description(),
                eventRequest.onlineLink(),
                eventRequest.startTime(),
                eventRequest.endTime(),
                eventRequest.workloadHours(),
                eventRequest.maxCapacity(),
                eventRequest.categoryId(),
                eventRequest.requirementIds(),
                eventRequest.tags(),
                null,
                eventRequest.speakers(),
                eventRequest.orgId(),
                eventRequest.daysBeforeNotify()
        );

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(organizerRepository.findById(org.getId())).thenReturn(Optional.of(org));
        when(organizerMemberRepository.existsByOrganizerIdAndUserId(org.getId(), organizer.getId())).thenReturn(true);
        when(eventRepository.existsByTitleIgnoreCaseAndOrganizerIdAndStartTime(any(), any(), any())).thenReturn(false);
        when(eventMapper.toEntity(any())).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toResponseDTO(any(), any())).thenReturn(eventResponse);

        EventResponseDTO response = eventService.createEvent(withoutLocation);

        assertThat(response).isNotNull();
        verify(locationRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve filtrar eventos prontos para notificar pela janela de tempo")
    void getReadyToNotifyEvents_FiltersByNotificationWindow() {
        Event shouldNotify = new Event();
        shouldNotify.setStartTime(LocalDateTime.now().plusDays(1).plusMinutes(30));
        shouldNotify.setDaysBeforeNotify(DaysBeforeNotify.ONE_DAY_BEFORE);

        Event shouldNotNotify = new Event();
        shouldNotNotify.setStartTime(LocalDateTime.now().plusDays(1).plusHours(5));
        shouldNotNotify.setDaysBeforeNotify(DaysBeforeNotify.ONE_DAY_BEFORE);

        when(eventRepository.findAllByStartTimeBetweenAndStatusNot(any(), any(), eq(EventStatus.COMPLETED)))
                .thenReturn(List.of(shouldNotify, shouldNotNotify));

        List<Event> result = eventService.getReadyToNotifyEvents();

        assertThat(result).containsExactly(shouldNotify);
    }

    @Test
    @DisplayName("Deve retornar contexto anonimo quando principal nao e User ao buscar evento por id")
    void getEventById_PrincipalNotUser_UsesAnonymousContext() {
        UUID id = event.getId();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("principal-invalido", null, List.of()));

        when(eventRepository.findById(id)).thenReturn(Optional.of(event));
        when(registrationRepository.countByEventIdAndStatus(id, "CONFIRMED")).thenReturn(2L);
        when(eventMapper.toResponseDTO(any(), any(), any())).thenReturn(eventResponse);

        EventResponseDTO response = eventService.getEventById(id);

        assertThat(response).isNotNull();
        verify(registrationRepository, never()).findByUserIdAndEventId(any(), any());
    }

    @Test
    @DisplayName("Deve lancar excecao de evento duplicado no patch quando titulo e horario mudam")
    void patchEvent_DuplicateAfterChanges_ThrowsEventAlreadyExistsException() {
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(Role.ADMIN);
        setAuthentication(admin);

        EventPatchRequestDTO patch = new EventPatchRequestDTO(
                "Novo titulo",
                null,
                null,
                event.getStartTime().plusDays(1),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(eventRepository.existsByTitleIgnoreCaseAndOrganizerIdAndStartTime(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> eventService.patchEvent(event.getId(), patch))
                .isInstanceOf(EventAlreadyExistsException.class);
    }

    // ==================== HELPERS ====================

    private void setAuthentication(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setAnonymousAuthentication() {
        SecurityContextHolder.clearContext();
        var auth = new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}

