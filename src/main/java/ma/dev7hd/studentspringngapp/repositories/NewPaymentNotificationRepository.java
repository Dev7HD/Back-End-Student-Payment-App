package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.NewPaymentNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewPaymentNotificationRepository extends JpaRepository<NewPaymentNotification, Long> {
    Optional<NewPaymentNotification> findByPaymentId(UUID id);
}
