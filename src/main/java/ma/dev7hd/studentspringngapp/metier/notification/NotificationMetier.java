package ma.dev7hd.studentspringngapp.metier.notification;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.*;
import ma.dev7hd.studentspringngapp.repositories.AdminRepository;
import ma.dev7hd.studentspringngapp.repositories.NewPaymentNotificationRepository;
import ma.dev7hd.studentspringngapp.repositories.NotificationRepository;
import ma.dev7hd.studentspringngapp.repositories.PendingStudentNotificationRepository;
import ma.dev7hd.studentspringngapp.websoket.config.WebSocketService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class NotificationMetier implements INotificationMetier {
    private final NotificationRepository notificationRepository;
    private final AdminRepository adminRepository;
    private final NewPaymentNotificationRepository newPaymentNotificationRepository;
    private final PendingStudentNotificationRepository pendingStudentNotificationRepository;
    private final WebSocketService webSocketService;

    @Override
    public void pushNotifications() throws ChangeSetPersister.NotFoundException {
        Admin admin = getCurrentAdmin();
        List<Notification> notifications = notificationRepository.findAllByAdminRemoverIsNot(admin);

        if (!notifications.isEmpty()) {
            notifications.forEach(notification -> {

                if (notification instanceof NewPaymentNotification) {
                    webSocketService.sendToSpecificUser("/notifications/new-payment", notification);
                } else if (notification instanceof PendingStudentNotification) {
                    webSocketService.sendToSpecificUser("/notifications/pending-registration", notification);
                }
            });
        }
    }


    @Override
    public void newNotification(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        if (saved instanceof NewPaymentNotification) webSocketService.sendToSpecificUser("/notifications/new-payment", saved);
        if (saved instanceof PendingStudentNotification) webSocketService.sendToSpecificUser("/notifications/pending-registration", saved);
    }

    @Override
    public void deleteNotification(@NotNull Long notificationId) throws ChangeSetPersister.NotFoundException {
        Admin admin = getCurrentAdmin();

        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (!notification.getAdminRemover().contains(admin)) {
                notification.getAdminRemover().add(admin);
            }
            notificationRepository.save(notification);
        });
    }

    @Override
    public void markAllAsRead(){
        notificationRepository.findAll().forEach(notification -> {
            if(!notification.isSeen()) notification.setSeen(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public boolean toggleSeen(Long id){
        Notification notification = notificationRepository.findById(id).orElse(null);
        if(notification == null) return false;
        notification.setSeen(!notification.isSeen());
        Notification saved = notificationRepository.save(notification);
        return saved.isSeen();
    }

    @Override
    public void notificationSeen(UUID paymentId, String email) {
        if (paymentId == null && email != null) {
            PendingStudentNotification notification = pendingStudentNotificationRepository.findByEmail(email).orElse(null);
            if (notification != null && !notification.isSeen()) {
                notification.setSeen(true);
                notificationRepository.save(notification);
            }
        } else if (paymentId != null && email == null) {
            NewPaymentNotification notification = newPaymentNotificationRepository.findByPaymentId(paymentId).orElse(null);
            if (notification != null && !notification.isSeen()) {
                notification.setSeen(true);
                notificationRepository.save(notification);
            }
        }
    }

    private Admin getCurrentAdmin() throws ChangeSetPersister.NotFoundException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminRepository.findById(userEmail).orElseThrow(ChangeSetPersister.NotFoundException::new);
    }
}
