package br.com.geac.backend.infrastucture.scheduler;

import br.com.geac.backend.aplication.services.EventService;
import br.com.geac.backend.aplication.services.NotificationService;
import br.com.geac.backend.aplication.services.RegistrationService;
import br.com.geac.backend.domain.entities.Event;
import br.com.geac.backend.domain.entities.Registration;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.infrastucture.repositories.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventAlertSchedulerTest {

    @Mock private EventService eventService;
    @Mock private EventRepository eventRepository;
    @Mock private RegistrationService registrationService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private EventAlertScheduler scheduler;

    @Test
    @DisplayName("checkCloseEvents deve nao fazer nada quando nao ha eventos")
    void checkCloseEvents_NoEvents() {
        when(eventService.getReadyToNotifyEvents()).thenReturn(List.of());

        scheduler.checkCloseEvents();

        verify(registrationService, never()).getUnotifiedRegistrationsById(any());
        verify(notificationService, never()).notifyAll(any(), any());
    }

    @Test
    @DisplayName("checkCloseEvents deve ignorar notificacao quando evento nao tem inscricoes")
    void checkCloseEvents_NoRegistrations() {
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("Event");
        when(eventService.getReadyToNotifyEvents()).thenReturn(List.of(event));
        when(registrationService.getUnotifiedRegistrationsById(event.getId())).thenReturn(List.of());

        scheduler.checkCloseEvents();

        verify(notificationService, never()).notifyAll(any(), any());
        verify(registrationService, never()).saveAll(any());
    }

    @Test
    @DisplayName("checkCloseEvents deve notificar usuarios e marcar inscricoes como notificadas")
    void checkCloseEvents_WithRegistrations() {
        Event event = new Event();
        event.setId(UUID.randomUUID());
        event.setTitle("Event");

        User u1 = new User();
        u1.setId(UUID.randomUUID());
        User u2 = new User();
        u2.setId(UUID.randomUUID());

        Registration r1 = new Registration();
        r1.setUser(u1);
        r1.setNotified(false);
        Registration r2 = new Registration();
        r2.setUser(u2);
        r2.setNotified(false);

        when(eventService.getReadyToNotifyEvents()).thenReturn(List.of(event));
        when(registrationService.getUnotifiedRegistrationsById(event.getId())).thenReturn(List.of(r1, r2));

        scheduler.checkCloseEvents();

        verify(notificationService).notifyAll(List.of(u1, u2), event);
        verify(registrationService).saveAll(List.of(r1, r2));
        assertThat(r1.isNotified()).isTrue();
        assertThat(r2.isNotified()).isTrue();
    }

    @Test
    @DisplayName("updateEventStatus deve delegar para o servico")
    void updateEventStatus_Success() {
        when(eventService.updateEventStatus(any(LocalDateTime.class))).thenReturn(3);

        scheduler.updateEventStatus();

        verify(eventService).updateEventStatus(any(LocalDateTime.class));
    }
}


