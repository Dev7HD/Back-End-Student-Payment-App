package ma.dev7hd.studentspringngapp.services.notification;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.NotificationDTO;
import ma.dev7hd.studentspringngapp.entities.Notification;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    void pushNotifications() throws ChangeSetPersister.NotFoundException;

    void newNotification(Notification notification);

    void deleteNotification(@NotNull Long notificationId) throws ChangeSetPersister.NotFoundException;

    void markAllAsRead();

    boolean toggleSeen(Long id);

    void notificationSeen(UUID paymentId, String email);

    Page<NotificationDTO> pageableNotifications(Boolean seen, int page, int size) throws ChangeSetPersister.NotFoundException;

    void deleteNotifications(List<Long> notificationIds) throws ChangeSetPersister.NotFoundException;

    void markNotificationsAsRead(List<Long> notificationIds);

    void toggleNotifications(List<Long> notificationIds);
}
