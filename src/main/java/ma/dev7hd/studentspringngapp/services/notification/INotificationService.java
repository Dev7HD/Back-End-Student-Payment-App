package ma.dev7hd.studentspringngapp.services.notification;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.NotificationDTO;
import ma.dev7hd.studentspringngapp.entities.notifications.admins.Notification;
import ma.dev7hd.studentspringngapp.entities.notifications.students.PaymentStatusChangedNotification;
import ma.dev7hd.studentspringngapp.entities.registrations.PendingStudent;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    void pushAdminNotifications() throws ChangeSetPersister.NotFoundException;

    void newAdminNotification(Notification notification);

    void deleteAdminNotification(@NotNull Long notificationId) throws ChangeSetPersister.NotFoundException;

    void markAllAdminNotificationsAsRead();

    boolean toggleAdminNotificationSeen(Long id);

    void adminNotificationSeen(UUID paymentId, String email);

    Page<NotificationDTO> pageableAdminNotifications(Boolean seen, int page, int size) throws ChangeSetPersister.NotFoundException;

    void deleteAdminNotifications(List<Long> notificationIds) throws ChangeSetPersister.NotFoundException;

    void markAdminNotificationsAsRead(List<Long> notificationIds);

    void toggleAdminNotifications(List<Long> notificationIds);

    void newStudentNotification(PaymentStatusChangedNotification paymentStatusChangedNotification, String studentEmail);

    void getStudentNotifications(String email);

    boolean toggleStudentNotificationSeen(Long id);

    void deleteStudentNotification(Long id);

    void studentNotificationSeen(Long id);

    UUID getPaymentIDAndMarkAsRead(Long id);

    Long getAdminNotificationsNonSeenCount() throws ChangeSetPersister.NotFoundException;

    Long getStudentNotificationsNonSeenCount() throws ChangeSetPersister.NotFoundException;

    void sendPendingStudentNotifications(PendingStudent pendingStudent);

    void seenNewRegistration(List<PendingStudent> pendingStudents);
}
