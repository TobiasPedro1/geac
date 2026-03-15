package br.com.geac.backend.api.config;

import br.com.geac.backend.aplication.dtos.request.RegisterRequestDTO;
import br.com.geac.backend.aplication.dtos.response.RegisterResponseDTO;
import br.com.geac.backend.aplication.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /auth/register deve permitir cadastro anonimo com filtros ativos")
    void register_AnonymousWithSecurityEnabled_Returns201() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Teste Security",
                "teste-security@email.com",
                "123456",
                "STUDENT"
        );
        RegisterResponseDTO response = new RegisterResponseDTO(
                "Teste Security",
                "teste-security@email.com",
                "STUDENT",
                "Cadastro realizado com sucesso!"
        );

        when(authService.registerUser(any())).thenReturn(response);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("teste-security@email.com"));
    }
}
