package br.com.geac.backend.aplication.services;

import br.com.geac.backend.domain.entities.Event;
import br.com.geac.backend.domain.entities.Location;
import br.com.geac.backend.domain.entities.Organizer;
import br.com.geac.backend.domain.entities.Registration;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.Campus;
import br.com.geac.backend.infrastucture.repositories.CertificateRepository;
import br.com.geac.backend.infrastucture.repositories.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock private CertificateRepository certificateRepository;
    @Mock private RegistrationRepository registrationRepository;

    @InjectMocks
    private CertificateService service;

    private UUID eventId;
    private Event event;
    private User user;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();

        event = new Event();
        event.setId(eventId);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("User");
    }

    @Test
    @DisplayName("issueCertificatesForEvent deve nao fazer nada quando nao ha participantes")
    void issueCertificatesForEvent_NoAttendees() {
        when(registrationRepository.findByEventId(eventId)).thenReturn(List.of());

        assertThatCode(() -> service.issueCertificatesForEvent(eventId)).doesNotThrowAnyException();

        verify(certificateRepository, never()).save(any());
    }

    @Test
    @DisplayName("issueCertificatesForEvent deve salvar certificado quando participante ainda nao tem certificado")
    void issueCertificatesForEvent_SaveNewCertificate() {
        Registration attended = new Registration();
        attended.setEvent(event);
        attended.setUser(user);
        attended.setAttended(true);
        Registration notAttended = new Registration();
        notAttended.setEvent(event);
        notAttended.setUser(user);
        notAttended.setAttended(false);

        when(registrationRepository.findByEventId(eventId)).thenReturn(List.of(attended, notAttended));
        when(certificateRepository.existsByUserIdAndEventId(user.getId(), eventId)).thenReturn(false);

        service.issueCertificatesForEvent(eventId);

        ArgumentCaptor<br.com.geac.backend.domain.entities.Certificate> captor =
                ArgumentCaptor.forClass(br.com.geac.backend.domain.entities.Certificate.class);
        verify(certificateRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getEvent()).isEqualTo(event);
        assertThat(captor.getValue().getValidationCode()).startsWith("GEAC-");
    }

    @Test
    @DisplayName("issueCertificatesForEvent deve pular salvamento quando certificado ja existe")
    void issueCertificatesForEvent_AlreadyExists() {
        Registration attended = new Registration();
        attended.setEvent(event);
        attended.setUser(user);
        attended.setAttended(true);

        when(registrationRepository.findByEventId(eventId)).thenReturn(List.of(attended));
        when(certificateRepository.existsByUserIdAndEventId(user.getId(), eventId)).thenReturn(true);

        service.issueCertificatesForEvent(eventId);

        verify(certificateRepository, never()).save(any());
    }

    @Test
    @DisplayName("downloadCertificatePdf deve retornar bytes gerados quando certificado existe")
    void downloadCertificatePdf_Success() {
        UUID userId = UUID.randomUUID();
        UUID certificateEventId = UUID.randomUUID();

        Event certEvent = new Event();
        certEvent.setId(certificateEventId);
        certEvent.setTitle("Event Title");
        certEvent.setWorkloadHours(4);
        certEvent.setStartTime(java.time.LocalDateTime.now().minusDays(2));
        certEvent.setEndTime(java.time.LocalDateTime.now().minusDays(1));
        Organizer organizer = new Organizer();
        organizer.setName("Organizer");
        certEvent.setOrganizer(organizer);
        Location location = new Location();
        location.setName("Main Hall");
        location.setCampus(Campus.CAMPUS_RECIFE_CENTRAL);
        certEvent.setLocation(location);

        br.com.geac.backend.domain.entities.Certificate certificate = new br.com.geac.backend.domain.entities.Certificate();
        User certUser = new User();
        certUser.setId(userId);
        certUser.setName("Student");
        certificate.setUser(certUser);
        certificate.setEvent(certEvent);
        certificate.setValidationCode("GEAC-ABC12345");

        when(certificateRepository.findByUserIdAndEventId(userId, certificateEventId))
                .thenReturn(java.util.Optional.of(certificate));

        byte[] pdf = service.downloadCertificatePdf(userId, certificateEventId);

        assertThat(pdf).isNotEmpty();
    }
}

