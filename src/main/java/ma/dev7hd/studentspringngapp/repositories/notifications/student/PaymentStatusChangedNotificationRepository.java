package ma.dev7hd.studentspringngapp.repositories.notifications.student;

import ma.dev7hd.studentspringngapp.entities.notifications.students.PaymentStatusChangedNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentStatusChangedNotificationRepository extends JpaRepository<PaymentStatusChangedNotification, Long> {
    List<PaymentStatusChangedNotification> findAllByStudentEmail(String email);

    @Query("SELECT COUNT(n) FROM PaymentStatusChangedNotification n WHERE n.seen = FALSE AND n.deleted = FALSE AND n.studentEmail = :studentEmail ")
    Long countStudentNonSeenNotifications(@Param("studentEmail") String studentEmail);
}
