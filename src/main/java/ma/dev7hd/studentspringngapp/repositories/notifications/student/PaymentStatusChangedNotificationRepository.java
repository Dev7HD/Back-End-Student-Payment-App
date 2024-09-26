package ma.dev7hd.studentspringngapp.repositories.notifications.student;

import ma.dev7hd.studentspringngapp.entities.notifications.students.PaymentStatusChangedNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentStatusChangedNotificationRepository extends JpaRepository<PaymentStatusChangedNotification, Long> {
}
