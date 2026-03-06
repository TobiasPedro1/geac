package br.com.geac.backend.aplication.services;

import br.com.geac.backend.aplication.dtos.response.NotificationResponseDTO;
import br.com.geac.backend.aplication.mappers.NotificationMapper;
import br.com.geac.backend.domain.entities.Event;
import br.com.geac.backend.domain.entities.Notification;
import br.com.geac.backend.domain.entities.User;
import br.com.geac.backend.domain.enums.DaysBeforeNotify;
import br.com.geac.backend.domain.exceptions.ConflictException;
import br.com.geac.backend.infrastucture.repositories.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private EmailService emailService;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService service;

    private User authenticatedUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        authenticatedUser = new User();
        authenticatedUser.setId(UUID.randomUUID());
        authenticatedUser.setEmail("user@test.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authenticatedUser, null, List.of())
        );

        notification = new Notification();
        notification.setId(1);
        notification.setUser(authenticatedUser);
        notification.setRead(false);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("notifyAll deve criar notificacoes usando mensagem de 24 horas")
    void notifyAll_OneDayBefore() {
        Event event = new Event();
        event.setTitle("Event 1");
        event.setDaysBeforeNotify(DaysBeforeNotify.ONE_DAY_BEFORE);

        User user = new User();
        user.setEmail("target@test.com");

        assertThatCode(() -> service.notifyAll(List.of(user), event)).doesNotThrowAnyException();

        verify(notificationRepository).save(any(Notification.class));
        verify(emailService).sendAlert("target@test.com", event);
    }

    @Test
    @DisplayName("notifyAll deve criar notificacoes usando mensagem de 7 dias")
    void notifyAll_OneWeekBefore() {
        Event event = new Event();
        event.setTitle("Event 2");
        event.setDaysBeforeNotify(DaysBeforeNotify.ONE_WEEK_BEFORE);

        User user = new User();
        user.setEmail("target@test.com");

        service.notifyAll(List.of(user), event);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("getUnreadNotifications deve mapear notificacoes")
    void getUnreadNotifications_Success() {
        NotificationResponseDTO dto = new NotificationResponseDTO(1, null, null, null, false, "TYPE", "Title", "Message", null);
        when(notificationRepository.findByUserIdAndIsRead(authenticatedUser.getId(), false)).thenReturn(List.of(notification));
        when(notificationMapper.toDTO(notification)).thenReturn(dto);

        List<NotificationResponseDTO> result = service.getUnreadNotifications();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Title");
    }

    @Test
    @DisplayName("getUnreadCount deve retornar quantidade de notificacoes nao lidas")
    void getUnreadCount_Success() {
        when(notificationRepository.findByUserIdAndIsRead(authenticatedUser.getId(), false))
                .thenReturn(List.of(notification, notification));

        Integer count = service.getUnreadCount();

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("markAsRead deve lancar excecao quando notificacao nao existe")
    void markAsRead_NotFound() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(10L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("markAsRead deve lancar excecao quando notificacao pertence a outro usuario")
    void markAsRead_OtherUser() {
        User other = new User();
        other.setId(UUID.randomUUID());
        notification.setUser(other);
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.markAsRead(10L))
                .isInstanceOf(ConflictException.class);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("markAsRead deve marcar e salvar notificacao")
    void markAsRead_Success() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        service.markAsRead(10L);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAllAsRead deve atualizar todas as notificacoes nao lidas")
    void markAllAsRead_Success() {
        Notification n1 = new Notification();
        n1.setUser(authenticatedUser);
        n1.setRead(false);
        Notification n2 = new Notification();
        n2.setUser(authenticatedUser);
        n2.setRead(false);
        when(notificationRepository.findByUserIdAndIsRead(authenticatedUser.getId(), false)).thenReturn(List.of(n1, n2));

        service.markAllAsRead();

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();
        verify(notificationRepository).saveAll(List.of(n1, n2));
    }
}


