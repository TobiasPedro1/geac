package br.com.geac.backend.Aplication.Services;

import br.com.geac.backend.Domain.Entities.Event;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class EmailService {

    private String remetente;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendAlert(String email, Event event) {
        if (mailSender == null) {
            log.warn("MailSender não configurado, email não enviado para: " + email);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remetente);
        message.setTo(email);
        message.setSubject("ALERT" + event.getTitle());
        message.setText(buildMessage(event));
        log.info("SENDING ALERT" + message.getText());
    }

    private String buildMessage(Event event) {
        return String.format(
                "Olá" + "o evento que voce se inscreveu: %s +, %s acontece em 24 horas. Data: %s/n, Local: %s/n",
                event.getTitle(), event.getDescription(), event.getStartTime(), event.getLocation()
        );
    }
}