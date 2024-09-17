package ma.dev7hd.studentspringngapp.metier.notification;

import ma.dev7hd.studentspringngapp.entities.Notification;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.UUID;

public interface INotificationMetier {
    void pushNotifications() throws ChangeSetPersister.NotFoundException;

    void newNotification(Notification notification);

    void deleteNotification(@NotNull Long notificationId) throws ChangeSetPersister.NotFoundException;

    void markAllAsRead();

    boolean toggleSeen(Long id);

    void notificationSeen(UUID paymentId, String email);
}
