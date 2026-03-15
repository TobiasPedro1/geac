package br.com.geac.backend.api.controller;

import br.com.geac.backend.aplication.dtos.response.OrganizerEventFeedbackResponseDTO;
import br.com.geac.backend.aplication.dtos.response.EvaluationResponseDTO;
import br.com.geac.backend.aplication.dtos.request.EvaluationRequestDTO;
import br.com.geac.backend.aplication.services.EvaluationService;
import br.com.geac.backend.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping("/evaluation")
@RequiredArgsConstructor
public class EvaluationController {
    private final EvaluationService evaluationService;
    @PostMapping
    public ResponseEntity<EvaluationResponseDTO> save(@RequestBody EvaluationRequestDTO evaluation,
                                                      @AuthenticationPrincipal User authenticatedUser){

        return  ResponseEntity.status(HttpStatus.CREATED).body(evaluationService.createEvaluation(evaluation,authenticatedUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<EvaluationResponseDTO>> findAllByEvent(@PathVariable UUID id){
        return ResponseEntity.ok(evaluationService.getEventEvaluations(id));
    }

    @GetMapping("/event/{id}/organizer")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<OrganizerEventFeedbackResponseDTO> findOrganizerEventFeedbacks(
            @PathVariable UUID id,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(evaluationService.getOrganizerEventFeedbacks(id, authenticatedUser));
    }
}
