package br.com.geac.backend.api.controller;

import br.com.geac.backend.aplication.dtos.response.CertificateResponseDTO;
import br.com.geac.backend.aplication.services.CertificateService;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.exceptions.CertificateNotAvailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/certificate")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/me")
    public ResponseEntity<List<CertificateResponseDTO>> getMyCertificates(Authentication authentication) {
        UUID userId = extractUserIdFromAuth(authentication);
        return ResponseEntity.ok(certificateService.getUserAvailableCertificates(userId));
    }

    @GetMapping(value = "/download/{eventId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable UUID eventId,
            Authentication authentication) {

        UUID userId = extractUserIdFromAuth(authentication);
        byte[] pdfBytes = certificateService.downloadCertificatePdf(userId, eventId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificado_" + eventId + ".pdf\"")
                .body(pdfBytes);
    }

    private UUID extractUserIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User userprincipal) {
            userprincipal = (User) authentication.getPrincipal();
            return userprincipal.getId();
        }
        throw new CertificateNotAvailableException("Acesso negado: Usuário não identificado.");
    }
}