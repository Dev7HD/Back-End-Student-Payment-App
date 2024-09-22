package ma.dev7hd.studentspringngapp.services.notification;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.NewPaymentNotificationDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.NotificationDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.PendingStudentNotificationDTO;
import ma.dev7hd.studentspringngapp.entities.*;
import ma.dev7hd.studentspringngapp.repositories.AdminRepository;
import ma.dev7hd.studentspringngapp.repositories.NewPaymentNotificationRepository;
import ma.dev7hd.studentspringngapp.repositories.NotificationRepository;
import ma.dev7hd.studentspringngapp.repositories.PendingStudentNotificationRepository;
import ma.dev7hd.studentspringngapp.websoket.config.WebSocketService;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final AdminRepository adminRepository;
    private final NewPaymentNotificationRepository newPaymentNotificationRepository;
    private final PendingStudentNotificationRepository pendingStudentNotificationRepository;
    private final WebSocketService webSocketService;
    private final ModelMapper modelMapper;

    @Override
    public void pushNotifications() throws ChangeSetPersister.NotFoundException {
        Admin admin = getCurrentAdmin();
        List<Notification> notifications = notificationRepository.findAllByAdminRemoverIsNot(admin);

        if (!notifications.isEmpty()) {
            notifications.forEach(this::pushNewNotification);
        }
    }

    @Override
    public void newNotification(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        pushNewNotification(saved);
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
    public boolean toggleSeen(Long id) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    notification.setSeen(!notification.isSeen());
                    return notificationRepository.save(notification).isSeen();
                })
                .orElse(false);
    }

    @Override
    public void notificationSeen(UUID paymentId, String email) {
        if (email != null) {
            pendingStudentNotificationRepository.findByEmail(email)
                    .filter(notification -> !notification.isSeen())
                    .ifPresent(notification -> {
                        notification.setSeen(true);
                        notificationRepository.save(notification);
                    });
            return;
        }

        if (paymentId != null) {
            newPaymentNotificationRepository.findByPaymentId(paymentId)
                    .filter(notification -> !notification.isSeen())
                    .ifPresent(notification -> {
                        notification.setSeen(true);
                        notificationRepository.save(notification);
                    });
            return;
        }

        throw new IllegalArgumentException("Either paymentId or email must be provided.");
    }

    @Override
    public Page<NotificationDTO> pageableNotifications(Boolean seen, int page, int size) throws ChangeSetPersister.NotFoundException {
        Admin currentAdmin = getCurrentAdmin();
        Page<Notification> notifications = notificationRepository.findAllWithPagination(currentAdmin, seen, PageRequest.of(page, size));

        return notifications.map(notification -> {
            if (notification instanceof NewPaymentNotification newPaymentNotification) {
                return modelMapper.map(newPaymentNotification, NewPaymentNotificationDTO.class);
            } else if (notification instanceof PendingStudentNotification pendingStudentNotification) {
                return modelMapper.map(pendingStudentNotification, PendingStudentNotificationDTO.class);
            } else {
                throw new IllegalArgumentException("Notification Error!");
            }
        });
    }

    @Override
    public void deleteNotifications(List<Long> notificationIds) throws ChangeSetPersister.NotFoundException {
        Admin admin = getCurrentAdmin();
        List<Notification> notifications = getNotificationsByIds(notificationIds).stream()
                .filter(notification -> !notification.getAdminRemover().contains(admin))
                .peek(notification -> notification.getAdminRemover().add(admin)).toList();

        if(!notifications.isEmpty()) notificationRepository.saveAll(notifications);
    }

    @Override
    public void markNotificationsAsRead(List<Long> notificationIds) {
        List<Notification> notifications = getNotificationsByIds(notificationIds).stream().filter(notification -> !notification.isSeen())
                .peek(notification -> notification.setSeen(true)).toList();
        if(!notifications.isEmpty()) notificationRepository.saveAll(notifications);
    }

    @Override
    public void toggleNotifications(List<Long> notificationIds){
        List<Notification> notifications = getNotificationsByIds(notificationIds);

        if (!notifications.isEmpty()) {
            notifications.forEach(notification -> notification.setSeen(!notification.isSeen()));
            notificationRepository.saveAll(notifications);
        }
    }

    /*private List<Notification> getNotificationsById(Long[] notificationIds){
        System.out.println("GET NOTIFICATIONS BY ID");
        List<Notification> allNotifications = new ArrayList<>();
        System.out.println("LIST INITIALISATION");
        for (Long notificationId : notificationIds) {
            System.out.println("GET NOTIFICATION BY ID: " + notificationId);
            notificationRepository.findById(notificationId).ifPresent(allNotifications::add);
        }
        System.out.println("NOTIFICATION SIZE: " + allNotifications.size());
        return allNotifications;
    }*/

    private List<Notification> getNotificationsByIds(List<Long> notificationIds) {
        return notificationRepository.findAllById(notificationIds);
    }

    private Admin getCurrentAdmin() throws ChangeSetPersister.NotFoundException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return adminRepository.findById(userEmail).orElseThrow(ChangeSetPersister.NotFoundException::new);
    }

    private void pushNewNotification(Notification notification) {
        String destination = null;
        Object notificationDTO = null;

        if (notification instanceof NewPaymentNotification) {
            notificationDTO = modelMapper.map(notification, NewPaymentNotificationDTO.class);
            destination = "/notifications/new-payment";
        } else if (notification instanceof PendingStudentNotification) {
            notificationDTO = modelMapper.map(notification, PendingStudentNotificationDTO.class);
            destination = "/notifications/pending-registration";
        }

        if (destination != null && notificationDTO != null) {
            webSocketService.sendToSpecificUser(destination, notificationDTO);
        }
    }
}
