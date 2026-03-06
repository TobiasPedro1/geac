package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.request.EvaluationRequestDTO;
import br.com.geac.backend.aplication.dtos.response.EvaluationResponseDTO;
import br.com.geac.backend.aplication.mappers.EvaluationMapper;
import br.com.geac.backend.domain.entities.Evaluation;
import br.com.geac.backend.domain.entities.Event;
import br.com.geac.backend.domain.entities.Registration;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.EventStatus;
import br.com.geac.backend.domain.exceptions.BadRequestException;
import br.com.geac.backend.domain.exceptions.EventNotFinishedException;
import br.com.geac.backend.domain.exceptions.EventNotFoundException;
import br.com.geac.backend.domain.exceptions.RegistrationNotFoundException;
import br.com.geac.backend.infrastucture.repositories.EvaluationRepository;
import br.com.geac.backend.infrastucture.repositories.EventRepository;
import br.com.geac.backend.infrastucture.repositories.RegistrationRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock private EvaluationRepository evaluationRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private EvaluationMapper mapper;

    @InjectMocks
    private EvaluationService service;

    private User user;
    private Event event;
    private Registration registration;
    private EvaluationRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.com");

        event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("Event");
        event.setStatus(EventStatus.COMPLETED);

        registration = new Registration();
        registration.setId(UUID.randomUUID());
        registration.setUser(user);
        registration.setEvent(event);
        registration.setAttended(true);

        requestDTO = new EvaluationRequestDTO(event.getId(), "Great", 5);
    }

    @Test
    @DisplayName("createEvaluation deve lancar excecao quando inscricao nao existe")
    void createEvaluation_RegistrationNotFound() {
        when(registrationRepository.findByUserIdAndEventId(user.getId(), event.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createEvaluation(requestDTO, user))
                .isInstanceOf(RegistrationNotFoundException.class);
    }

    @Test
    @DisplayName("createEvaluation deve lancar excecao quando inscricao pertence a outro usuario")
    void createEvaluation_NotOwner() {
        User other = new User();
        other.setId(UUID.randomUUID());
        registration.setUser(other);
        when(registrationRepository.findByUserIdAndEventId(user.getId(), event.getId())).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> service.createEvaluation(requestDTO, user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("createEvaluation deve lancar excecao quando usuario nao participou do evento")
    void createEvaluation_NotAttended() {
        registration.setAttended(false);
        when(registrationRepository.findByUserIdAndEventId(user.getId(), event.getId())).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> service.createEvaluation(requestDTO, user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("createEvaluation deve lancar excecao quando evento nao esta concluido")
    void createEvaluation_EventNotFinished() {
        event.setStatus(EventStatus.ACTIVE);
        when(registrationRepository.findByUserIdAndEventId(user.getId(), event.getId())).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> service.createEvaluation(requestDTO, user))
                .isInstanceOf(EventNotFinishedException.class);
    }

    @Test
    @DisplayName("createEvaluation deve lancar excecao quando avaliacao ja existe")
    void createEvaluation_AlreadyExists() {
        when(registrationRepository.findByUserIdAndEventId(user.getId(), event.getId())).thenReturn(Optional.of(registration));
        when(evaluationRepository.existsByRegistrationId(registration.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.createEvaluation(requestDTO, user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("createEvaluation deve salvar e mapear resposta")
    void createEvaluation_Success() {
        Evaluation saved = new Evaluation();
        saved.setId(1L);
        saved.setRegistration(registration);
        saved.setRating(5);
        saved.setComment("Great");
        saved.setCreatedAt(LocalDateTime.now());
        EvaluationResponseDTO response = new EvaluationResponseDTO(
                1L, registration.getId(), event.getId(), "Event", user.getId(), "User", 5, "Great", saved.getCreatedAt()
        );

        when(registrationRepository.findByUserIdAndEventId(user.getId(), event.getId())).thenReturn(Optional.of(registration));
        when(evaluationRepository.existsByRegistrationId(registration.getId())).thenReturn(false);
        when(evaluationRepository.save(any(Evaluation.class))).thenReturn(saved);
        when(mapper.toDTO(saved)).thenReturn(response);

        EvaluationResponseDTO result = service.createEvaluation(requestDTO, user);

        ArgumentCaptor<Evaluation> captor = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertThat(captor.getValue().getRegistration()).isEqualTo(registration);
        assertThat(result.eventId()).isEqualTo(event.getId());
    }

    @Test
    @DisplayName("getEventEvaluations deve lancar excecao quando evento nao existe")
    void getEventEvaluations_EventNotFound() {
        UUID eventId = event.getId();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEventEvaluations(eventId))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("getEventEvaluations deve mapear avaliacoes")
    void getEventEvaluations_Success() {
        UUID eventId = event.getId();
        Evaluation eval = new Evaluation();
        EvaluationResponseDTO response = new EvaluationResponseDTO(1L, registration.getId(), eventId, "Event", user.getId(), "User", 5, "Great", LocalDateTime.now());
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(evaluationRepository.findAllByRegistrationEvent(event)).thenReturn(List.of(eval));
        when(mapper.toDTO(eval)).thenReturn(response);

        List<EvaluationResponseDTO> result = service.getEventEvaluations(eventId);

        assertThat(result).hasSize(1);
    }
}

