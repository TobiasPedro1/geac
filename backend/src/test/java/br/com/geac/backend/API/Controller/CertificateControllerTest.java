package br.com.geac.backend.api.controller;

import br.com.geac.backend.aplication.dtos.response.CertificateResponseDTO;
import br.com.geac.backend.aplication.services.CertificateService;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.exceptions.CertificateNotAvailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateControllerTest {

    @Mock private CertificateService certificateService;
    @Mock private Authentication authentication;

    @InjectMocks
    private CertificateController controller;

    @Test
    @DisplayName("getMyCertificates deve retornar certificados quando principal autenticado e User")
    void getMyCertificates_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        CertificateResponseDTO dto = CertificateResponseDTO.builder()
                .certificateId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .eventTitle("Event")
                .build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(certificateService.getUserAvailableCertificates(userId)).thenReturn(List.of(dto));

        ResponseEntity<List<CertificateResponseDTO>> response = controller.getMyCertificates(authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("downloadCertificate deve retornar bytes de pdf e header de anexo")
    void downloadCertificate_Success() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        byte[] pdf = new byte[]{1, 2, 3};

        when(authentication.getPrincipal()).thenReturn(user);
        when(certificateService.downloadCertificatePdf(userId, eventId)).thenReturn(pdf);

        ResponseEntity<byte[]> response = controller.downloadCertificate(eventId, authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(pdf);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("certificado_" + eventId);
    }

    @Test
    @DisplayName("metodos do controller devem lancar excecao quando principal e invalido")
    void methods_InvalidPrincipal() {
        when(authentication.getPrincipal()).thenReturn("anonymous");

        assertThatThrownBy(() -> controller.getMyCertificates(authentication))
                .isInstanceOf(CertificateNotAvailableException.class);
        assertThatThrownBy(() -> controller.downloadCertificate(UUID.randomUUID(), authentication))
                .isInstanceOf(CertificateNotAvailableException.class);
    }
}


