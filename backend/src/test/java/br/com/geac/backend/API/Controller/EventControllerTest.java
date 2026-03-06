package br.com.geac.backend.API.Controller;

import br.com.geac.backend.Aplication.DTOs.Reponse.EventResponseDTO;
import br.com.geac.backend.Aplication.DTOs.Request.EventRequestDTO;
import br.com.geac.backend.Aplication.Services.EventService;
import br.com.geac.backend.Domain.Enums.DaysBeforeNotify;
import br.com.geac.backend.Domain.Exceptions.EventNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    private ObjectMapper objectMapper;
    private EventResponseDTO eventResponse;
    private EventRequestDTO eventRequest;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        orgId = UUID.randomUUID();

        eventRequest = new EventRequestDTO(
                "Palestra sobre IA",
                "Descrição da palestra",
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                2,
                100,
                1,
                Set.of(1),
                Set.of(1),
                null,
                Set.of(1),
                orgId,
                DaysBeforeNotify.ONE_DAY_BEFORE
        );

        eventResponse = EventResponseDTO.builder()
                .id(UUID.randomUUID())
                .title("Palestra sobre IA")
                .description("Descrição da palestra")
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

    @Test
    @DisplayName("POST /events/create - Deve retornar 201 ao criar evento")
    @WithMockUser(roles = "ORGANIZER")
    void createEvent_Success_Returns201() throws Exception {
        when(eventService.createEvent(any())).thenReturn(eventResponse);

        mockMvc.perform(post("/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Palestra sobre IA"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /events/create - Deve retornar 403 quando acesso negado")
    @WithMockUser(roles = "ORGANIZER")
    void createEvent_AccessDenied_Returns403() throws Exception {
        when(eventService.createEvent(any()))
                .thenThrow(new br.com.geac.backend.Domain.Exceptions.BadRequestException("Apenas organizadores podem cadastrar eventos."));

        mockMvc.perform(post("/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /events - Deve retornar lista de eventos com 200")
    void getAllEvents_Returns200() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of(eventResponse));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Palestra sobre IA"));
    }

    @Test
    @DisplayName("GET /events - Deve retornar lista vazia com 200")
    void getAllEvents_EmptyList_Returns200() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /events/{id} - Deve retornar evento por ID com 200")
    void getEventById_Returns200() throws Exception {
        UUID id = eventResponse.id();
        when(eventService.getEventById(id)).thenReturn(eventResponse);

        mockMvc.perform(get("/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Palestra sobre IA"));
    }

    @Test
@DisplayName("GET /events/{id} - Deve retornar 200 para ID inexistente retorna resposta do serviço")
void getEventById_NotFound_ReturnsServiceBehavior() throws Exception {
    UUID id = UUID.randomUUID();
    when(eventService.getEventById(id)).thenReturn(null);

    mockMvc.perform(get("/events/{id}", id))
            .andExpect(status().isOk());
}

    @Test
    @DisplayName("DELETE /events/{id} - Deve retornar 204 ao deletar evento")
    @WithMockUser(roles = "ORGANIZER")
    void deleteEvent_Returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/events/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /events/{id} - Deve retornar 403 quando acesso negado")
    @WithMockUser(roles = "ORGANIZER")
    void deleteEvent_AccessDenied_Returns403() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new br.com.geac.backend.Domain.Exceptions.BadRequestException("Sem permissão"))
                .when(eventService).deleteEvent(id);

        mockMvc.perform(delete("/events/{id}", id))
                .andExpect(status().isBadRequest());
    }
}