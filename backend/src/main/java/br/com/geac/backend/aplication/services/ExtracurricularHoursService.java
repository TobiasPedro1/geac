package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.request.StudentHoursResponseDTO;
import br.com.geac.backend.aplication.mappers.StudentHoursMapper;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.exceptions.UserNotFoundException;
import br.com.geac.backend.infrastucture.repositories.StudentExtracurricularHoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExtracurricularHoursService {

    private final StudentExtracurricularHoursRepository repository;
    private final StudentHoursMapper mapper;

    // Retorna as horas do usuário logado no momento
    public StudentHoursResponseDTO getMyHours() {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getHoursByStudentId(loggedUser.getId());
    }

    // Retorna as horas de um aluno específico pelo ID (útil para admins/professores)
    public StudentHoursResponseDTO getHoursByStudentId(UUID studentId) {
        var hours = repository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException("Dados de horas não encontrados para o aluno especificado."));
        return mapper.toResponseDTO(hours);
    }

    // Retorna as horas de todos os alunos (para painel administrativo)
    public List<StudentHoursResponseDTO> getAllStudentHours() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }
}