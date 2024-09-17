package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.PendingStudentNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingStudentNotificationRepository extends JpaRepository<PendingStudentNotification, Long> {
    Optional<PendingStudentNotification> findByEmail(String email);
}
