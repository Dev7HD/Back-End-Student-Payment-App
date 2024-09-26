package ma.dev7hd.studentspringngapp.repositories.notifications.admin;

import ma.dev7hd.studentspringngapp.entities.notifications.admins.PendingStudentNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingStudentNotificationRepository extends JpaRepository<PendingStudentNotification, Long> {
    Optional<PendingStudentNotification> findByEmail(String email);
}
