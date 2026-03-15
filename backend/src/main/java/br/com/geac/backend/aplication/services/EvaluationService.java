package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.request.EvaluationRequestDTO;
import br.com.geac.backend.aplication.dtos.response.EvaluationResponseDTO;
import br.com.geac.backend.aplication.dtos.response.OrganizerEventFeedbackResponseDTO;
import br.com.geac.backend.aplication.mappers.EvaluationMapper;
import br.com.geac.backend.domain.entities.Evaluation;
import br.com.geac.backend.domain.entities.Event;
import br.com.geac.backend.domain.entities.Registration;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.EventStatus;
import br.com.geac.backend.domain.enums.Role;
import br.com.geac.backend.domain.exceptions.BadRequestException;
import br.com.geac.backend.domain.exceptions.EventNotFinishedException;
import br.com.geac.backend.domain.exceptions.EventNotFoundException;
import br.com.geac.backend.domain.exceptions.RegistrationNotFoundException;
import br.com.geac.backend.infrastucture.repositories.EvaluationRepository;
import br.com.geac.backend.infrastucture.repositories.EventRepository;
import br.com.geac.backend.infrastucture.repositories.OrganizerMemberRepository;
import br.com.geac.backend.infrastucture.repositories.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class EvaluationService {
    private final EvaluationRepository evaluationRepository;
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final OrganizerMemberRepository organizerMemberRepository;
    private final EvaluationMapper mapper;

    public EvaluationResponseDTO createEvaluation(EvaluationRequestDTO dto, User authenticatedUser) {

        Registration registration = registrationRepository.findByUserIdAndEventId(authenticatedUser.getId(),dto.eventId())
                .orElseThrow(()-> new RegistrationNotFoundException("A inscrição não foi encontrada"));
        if (!registration.getUser().getId().equals(authenticatedUser.getId())) {
            throw new BadRequestException("Você está tentando avaliar uma inscrição que nao é sua");
        }
        if(!registration.getAttended()){
            throw new BadRequestException("Você não pode avaliar um evento que não compareceu");
        }
        if(!registration.getEvent().getStatus().equals(EventStatus.COMPLETED)){
            throw new EventNotFinishedException("Voce só pode avaliar um evento que acabou");
        }
        if(evaluationRepository.existsByRegistrationId((registration.getId()))){
            throw new BadRequestException("Voce já avaliou este evento");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setCreatedAt(LocalDateTime.now());
        evaluation.setRegistration(registration);
        evaluation.setComment(dto.comment());
        evaluation.setRating(dto.rating());
        var saved = evaluationRepository.save(evaluation);

        log.info("Avaliação {} criada pelo usuário {} para o evento {}",
                saved.getId(), authenticatedUser.getUsername(), dto.eventId());
        return mapper.toDTO(saved);

    }
    public List<EvaluationResponseDTO> getEventEvaluations(UUID eventId){
        Event relatedEvent = getEventByIdOrThrow(eventId);

        return  evaluationRepository.findAllByRegistrationEvent(relatedEvent)
                .stream()
                .map(mapper::toDTO)
                .toList();

    }

    public OrganizerEventFeedbackResponseDTO getOrganizerEventFeedbacks(UUID eventId, User authenticatedUser) {
        Event event = getEventByIdOrThrow(eventId);
        validateOrganizerAccess(event, authenticatedUser);

        List<EvaluationResponseDTO> feedbacks = evaluationRepository.findAllByRegistrationEvent(event)
                .stream()
                .map(mapper::toDTO)
                .sorted(Comparator.comparing(EvaluationResponseDTO::createdAt).reversed())
                .toList();

        double averageRating = feedbacks.stream()
                .mapToInt(EvaluationResponseDTO::rating)
                .average()
                .orElse(0.0);

        return new OrganizerEventFeedbackResponseDTO(
                event.getId(),
                event.getTitle(),
                averageRating,
                feedbacks.size(),
                feedbacks
        );
    }

    private Event getEventByIdOrThrow(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Evento nao encontrado"));
    }

    private void validateOrganizerAccess(Event event, User authenticatedUser) {
        if (authenticatedUser.getRole() == Role.ADMIN) {
            return;
        }

        boolean isOrganizerMember = organizerMemberRepository.existsByOrganizerIdAndUserId(
                event.getOrganizer().getId(),
                authenticatedUser.getId()
        );

        if (!isOrganizerMember) {
            throw new AccessDeniedException("Você não tem permissão para visualizar os feedbacks deste evento.");
        }
    }
}
