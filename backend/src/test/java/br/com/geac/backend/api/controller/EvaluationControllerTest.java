package br.com.geac.backend.api.controller;

import br.com.geac.backend.aplication.dtos.response.EvaluationResponseDTO;
import br.com.geac.backend.aplication.dtos.response.OrganizerEventFeedbackResponseDTO;
import br.com.geac.backend.aplication.services.EvaluationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluationService evaluationService;

    @Test
    @DisplayName("GET /evaluation/event/{id}/organizer - Deve retornar feedbacks do organizador com 200")
    @WithMockUser(roles = "ORGANIZER")
    void findOrganizerEventFeedbacks_Returns200() throws Exception {
        UUID eventId = UUID.randomUUID();
        OrganizerEventFeedbackResponseDTO response = new OrganizerEventFeedbackResponseDTO(
                eventId,
                "Evento Teste",
                4.5d,
                1,
                List.of(new EvaluationResponseDTO(
                        1L,
                        UUID.randomUUID(),
                        eventId,
                        "Evento Teste",
                        UUID.randomUUID(),
                        "Participante",
                        5,
                        "Excelente organizacao",
                        LocalDateTime.now()
                ))
        );

        when(evaluationService.getOrganizerEventFeedbacks(eq(eventId), any())).thenReturn(response);

        mockMvc.perform(get("/evaluation/event/{id}/organizer", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventTitle").value("Evento Teste"))
                .andExpect(jsonPath("$.averageRating").value(4.5d))
                .andExpect(jsonPath("$.totalFeedbacks").value(1))
                .andExpect(jsonPath("$.feedbacks[0].comment").value("Excelente organizacao"));
    }

    @Test
    @DisplayName("GET /evaluation/event/{id}/organizer - Deve retornar 403 para usuario sem papel permitido")
    @WithMockUser(roles = "STUDENT")
    void findOrganizerEventFeedbacks_ForbiddenRole_Returns403() throws Exception {
        mockMvc.perform(get("/evaluation/event/{id}/organizer", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
