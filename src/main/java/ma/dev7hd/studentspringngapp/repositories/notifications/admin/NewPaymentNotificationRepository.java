package ma.dev7hd.studentspringngapp.repositories.notifications.admin;

import ma.dev7hd.studentspringngapp.entities.notifications.admins.NewPaymentNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewPaymentNotificationRepository extends JpaRepository<NewPaymentNotification, Long> {
    Optional<NewPaymentNotification> findByPaymentId(UUID id);
}
