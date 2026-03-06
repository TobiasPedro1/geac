package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.AuthResponseDTO;
import br.com.geac.backend.aplication.dtos.response.RegisterResponseDTO;
import br.com.geac.backend.aplication.dtos.request.AuthRequestDTO;
import br.com.geac.backend.aplication.dtos.request.RegisterRequestDTO;
import br.com.geac.backend.aplication.mappers.UserMapper;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.exceptions.EmailAlreadyExistsException;
import br.com.geac.backend.infrastucture.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public RegisterResponseDTO registerUser(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("O Email já está em uso");
        }

        User user = userMapper.registerToUser(request);

        String encriptedPass = encoder.encode(request.password());
        user.setPassword(encriptedPass);

        return userMapper.userToRegisterResponse(userRepository.save(user));
    }

    public AuthResponseDTO login(AuthRequestDTO data) {

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return new AuthResponseDTO(token);
    }

    public void logout() {
        // Em uma aplicação com sessões stateless (JWT), o logout é feito principalmente no cliente
        // removendo o token. Este métod pode ser estendido para implementar blacklist de tokens
        // ou outras operações de limpeza necessárias
    }
}
