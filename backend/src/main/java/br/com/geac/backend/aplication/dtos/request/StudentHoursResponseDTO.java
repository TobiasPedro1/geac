package br.com.geac.backend.aplication.dtos.request;

import java.util.UUID;

public record StudentHoursResponseDTO(
        UUID studentId,
        String studentName,
        String studentEmail,
        Long totalCertificadosEmitidos,
        Long totalHorasAcumuladas
) {
}
