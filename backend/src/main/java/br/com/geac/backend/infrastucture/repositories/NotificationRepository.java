package br.com.geac.backend.infrastucture.repositories;

import br.com.geac.backend.domain.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository  extends JpaRepository<Notification, Long> {


    List<Notification> findByUserIdAndIsRead(UUID userId, boolean isRead);
}
