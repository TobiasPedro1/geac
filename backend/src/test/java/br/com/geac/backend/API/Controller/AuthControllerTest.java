package br.com.geac.backend.api.controller;

import br.com.geac.backend.aplication.dtos.response.AuthResponseDTO;
import br.com.geac.backend.aplication.dtos.response.RegisterResponseDTO;
import br.com.geac.backend.aplication.dtos.request.AuthRequestDTO;
import br.com.geac.backend.aplication.dtos.request.RegisterRequestDTO;
import br.com.geac.backend.aplication.services.AuthService;
import br.com.geac.backend.domain.exceptions.EmailAlreadyExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 201 ao registrar usuario")
    void register_Success_Returns201() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Douglas Henrique", "douglas@email.com", "senha123", "ORGANIZER"
        );
        RegisterResponseDTO response = new RegisterResponseDTO(
                "Douglas Henrique", "douglas@email.com", "ORGANIZER", "Cadastro realizado com sucesso!"
        );
        when(authService.registerUser(any())).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("douglas@email.com"))
                .andExpect(jsonPath("$.name").value("Douglas Henrique"));
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 409 quando email ja existe")
    void register_EmailAlreadyExists_Returns409() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Douglas", "douglas@email.com", "senha123", "STUDENT"
        );
        when(authService.registerUser(any()))
                .thenThrow(new EmailAlreadyExistsException("O Email jÃ¡ estÃ¡ em uso"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 400 quando dados invalidos")
    void register_InvalidData_Returns400() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("", "", "123", "");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 200 com token ao fazer login")
    void login_Success_Returns200() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO("douglas@email.com", "senha123");
        AuthResponseDTO response = new AuthResponseDTO("jwt-token-gerado");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-gerado"));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 401 com credenciais invalidas")
    void login_InvalidCredentials_Returns401() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO("douglas@email.com", "senhaErrada");
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Credenciais invÃ¡lidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/logout - Deve retornar 204")
    void logout_Returns204() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent());
    }
}

