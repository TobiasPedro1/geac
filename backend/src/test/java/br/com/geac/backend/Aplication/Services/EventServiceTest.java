package br.com.geac.backend.Aplication.Services;

import br.com.geac.backend.Aplication.DTOs.Reponse.EventResponseDTO;
import br.com.geac.backend.Aplication.DTOs.Request.EventRequestDTO;
import br.com.geac.backend.Aplication.Mappers.EventMapper;
import br.com.geac.backend.Domain.Entities.*;
import br.com.geac.backend.Domain.Enums.DaysBeforeNotify;
import br.com.geac.backend.Domain.Enums.EventStatus;
import br.com.geac.backend.Domain.Enums.Role;
import br.com.geac.backend.Domain.Exceptions.EventNotFoundException;
import br.com.geac.backend.Infrastructure.Repositories.*;
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
        organizer.setEmail("org@ufape.br");
        organizer.setName("Organizador Teste");
        organizer.setRole(Role.ORGANIZER);

        student = new User();
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
        event.setStatus(EventStatus.ACTIVE);
        event.setOrganizer(org);
        event.setCategory(category);
        event.setSpeakers(new HashSet<>());
        event.setTags(new HashSet<>());
        event.setRequirements(new HashSet<>());

        eventRequest = new EventRequestDTO(
                "Palestra sobre IA",
                "Uma palestra sobre inteligência artificial",
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
    @DisplayName("Deve criar evento com sucesso quando usuário é ORGANIZER")
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
    @DisplayName("Deve lançar AccessDeniedException quando usuário é STUDENT")
    void createEvent_StudentUser_ThrowsAccessDeniedException() {
        setAuthentication(student);

        assertThatThrownBy(() -> eventService.createEvent(eventRequest))
                .isInstanceOf(AccessDeniedException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando categoria não encontrada")
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
    @DisplayName("Deve retornar lista vazia quando não há eventos")
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
    @DisplayName("Deve lançar exceção quando evento não encontrado por ID")
    void getEventById_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(id))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Evento não encontrado");
    }

    // ==================== DELETE EVENT ====================

    @Test
    @DisplayName("Deve deletar evento com sucesso quando usuário é ADMIN")
    void deleteEvent_AdminUser_Success() {
        User admin = new User();
        admin.setRole(Role.ADMIN);
        setAuthentication(admin);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThatCode(() -> eventService.deleteEvent(event.getId())).doesNotThrowAnyException();
        verify(eventRepository).delete(event);
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