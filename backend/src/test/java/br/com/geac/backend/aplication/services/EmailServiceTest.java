package br.com.geac.backend.aplication.services;

import br.com.geac.backend.domain.entities.Event;
import br.com.geac.backend.domain.entities.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class EmailServiceTest {

    @Test
    @DisplayName("sendAlert deve retornar cedo quando mailSender e null")
    void sendAlert_MailSenderNull() {
        EmailService service = new EmailService("noreply@test.com", null);
        Event event = baseEvent();

        assertThatCode(() -> service.sendAlert("user@test.com", event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendAlert deve montar mensagem quando mailSender existe")
    void sendAlert_MailSenderPresent() {
        EmailService service = new EmailService("noreply@test.com", mock(JavaMailSender.class));
        Event event = baseEvent();

        assertThatCode(() -> service.sendAlert("user@test.com", event))
                .doesNotThrowAnyException();
    }

    private static Event baseEvent() {
        Event event = new Event();
        event.setTitle("Event");
        event.setDescription("Description");
        event.setStartTime(LocalDateTime.now().plusDays(1));
        event.setLocation(new Location());
        return event;
    }
}


