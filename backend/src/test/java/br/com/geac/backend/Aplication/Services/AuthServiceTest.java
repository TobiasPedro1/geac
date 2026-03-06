package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.AuthResponseDTO;
import br.com.geac.backend.aplication.dtos.response.RegisterResponseDTO;
import br.com.geac.backend.aplication.dtos.request.AuthRequestDTO;
import br.com.geac.backend.aplication.dtos.request.RegisterRequestDTO;
import br.com.geac.backend.aplication.mappers.UserMapper;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.Role;
import br.com.geac.backend.domain.exceptions.EmailAlreadyExistsException;
import br.com.geac.backend.infrastucture.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder encoder;
    @Mock private TokenService tokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequestDTO registerRequest;
    private AuthRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("douglas@email.com");
        user.setName("Douglas Henrique");
        user.setPassword("encodedPassword");
        user.setRole(Role.ORGANIZER);

        registerRequest = new RegisterRequestDTO(
                "Douglas Henrique", "douglas@email.com", "senha123", "ORGANIZER"
        );

        loginRequest = new AuthRequestDTO("douglas@email.com", "senha123");
    }

    @Test
    @DisplayName("Deve registrar usuario com sucesso")
    void registerUser_Success() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userMapper.registerToUser(registerRequest)).thenReturn(user);
        when(encoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.userToRegisterResponse(user)).thenReturn(
                new RegisterResponseDTO(user.getName(), user.getEmail(), user.getRole().toString(), "Cadastro realizado com sucesso!")
        );

        RegisterResponseDTO response = authService.registerUser(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("douglas@email.com");
        verify(userRepository).save(any(User.class));
        verify(encoder).encode("senha123");
    }

    @Test
    @DisplayName("Deve lancar excecao quando email ja existe")
    void registerUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .isInstanceOf(Exception.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve fazer login com sucesso e retornar token")
    void login_Success() {
        var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(tokenService.generateToken(user)).thenReturn("jwt-token-gerado");

        AuthResponseDTO response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token-gerado");
        verify(tokenService).generateToken(user);
    }

    @Test
    @DisplayName("Deve chamar encoder ao registrar usuario")
    void registerUser_ShouldEncodePassword() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.registerToUser(any())).thenReturn(user);
        when(encoder.encode("senha123")).thenReturn("$2a$hash");
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.userToRegisterResponse(any())).thenReturn(
                new RegisterResponseDTO("Douglas", "douglas@email.com", "ORGANIZER", "Cadastro realizado com sucesso!")
        );

        authService.registerUser(registerRequest);

        verify(encoder).encode("senha123");
    }

    @Test
    @DisplayName("Deve executar logout sem erros")
    void logout_Success() {
        assertThatCode(() -> authService.logout()).doesNotThrowAnyException();
    }
}

