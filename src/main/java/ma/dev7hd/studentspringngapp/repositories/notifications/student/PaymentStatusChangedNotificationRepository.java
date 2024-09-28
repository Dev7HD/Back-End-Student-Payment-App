package ma.dev7hd.studentspringngapp.repositories.notifications.student;

import ma.dev7hd.studentspringngapp.entities.notifications.students.PaymentStatusChangedNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentStatusChangedNotificationRepository extends JpaRepository<PaymentStatusChangedNotification, Long> {
    List<PaymentStatusChangedNotification> findAllByStudentEmail(String email);
}
