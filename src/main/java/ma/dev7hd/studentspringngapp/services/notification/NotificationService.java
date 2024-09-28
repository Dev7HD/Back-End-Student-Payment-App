package ma.dev7hd.studentspringngapp.services.notification;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.PaymentStatusChangedNotificationDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.NewPaymentNotificationDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.NotificationDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.PendingStudentNotificationDTO;
import ma.dev7hd.studentspringngapp.entities.notifications.admins.NewPaymentNotification;
import ma.dev7hd.studentspringngapp.entities.notifications.admins.Notification;
import ma.dev7hd.studentspringngapp.entities.notifications.admins.PendingStudentNotification;
import ma.dev7hd.studentspringngapp.entities.notifications.students.PaymentStatusChangedNotification;
import ma.dev7hd.studentspringngapp.entities.users.Admin;
import ma.dev7hd.studentspringngapp.repositories.notifications.student.PaymentStatusChangedNotificationRepository;
import ma.dev7hd.studentspringngapp.repositories.users.AdminRepository;
import ma.dev7hd.studentspringngapp.repositories.notifications.admin.NewPaymentNotificationRepository;
import ma.dev7hd.studentspringngapp.repositories.notifications.admin.NotificationRepository;
import ma.dev7hd.studentspringngapp.repositories.notifications.admin.PendingStudentNotificationRepository;
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
    private final PaymentStatusChangedNotificationRepository paymentStatusChangedNotificationRepository;

    @Override
    public void pushAdminNotifications() throws ChangeSetPersister.NotFoundException {
        Admin admin = getCurrentAdmin();
        List<Notification> notifications = notificationRepository.findAllByAdminRemoverIsNot(admin);

        if (!notifications.isEmpty()) {
            notifications.forEach(this::pushNewNotification);
        }
    }

    @Override
    public void newAdminNotification(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        pushNewNotification(saved);
    }

    @Override
    public void deleteAdminNotification(@NotNull Long notificationId) throws ChangeSetPersister.NotFoundException {
        Admin admin = getCurrentAdmin();

        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (!notification.getAdminRemover().contains(admin)) {
                notification.getAdminRemover().add(admin);
            }
            notificationRepository.save(notification);
        });
    }

    @Override
    public void markAllAdminNotificationsAsRead(){
        notificationRepository.findAll().forEach(notification -> {
            if(!notification.isSeen()) notification.setSeen(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public boolean toggleAdminNotificationSeen(Long id) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    notification.setSeen(!notification.isSeen());
                    return notificationRepository.save(notification).isSeen();
                })
                .orElse(false);
    }

    @Override
    public void adminNotificationSeen(UUID paymentId, String email) {
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
    public Page<NotificationDTO> pageableAdminNotifications(Boolean seen, int page, int size) throws ChangeSetPersister.NotFoundException {
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
    public void deleteAdminNotifications(List<Long> notificationIds) throws ChangeSetPersister.NotFoundException {
        Admin admin = getCurrentAdmin();
        List<Notification> notifications = getNotificationsByIds(notificationIds).stream()
                .filter(notification -> !notification.getAdminRemover().contains(admin))
                .peek(notification -> notification.getAdminRemover().add(admin)).toList();

        if(!notifications.isEmpty()) notificationRepository.saveAll(notifications);
    }

    @Override
    public void markAdminNotificationsAsRead(List<Long> notificationIds) {
        List<Notification> notifications = getNotificationsByIds(notificationIds).stream().filter(notification -> !notification.isSeen())
                .peek(notification -> notification.setSeen(true)).toList();
        if(!notifications.isEmpty()) notificationRepository.saveAll(notifications);
    }

    @Override
    public void toggleAdminNotifications(List<Long> notificationIds){
        List<Notification> notifications = getNotificationsByIds(notificationIds);

        if (!notifications.isEmpty()) {
            notifications.forEach(notification -> notification.setSeen(!notification.isSeen()));
            notificationRepository.saveAll(notifications);
        }
    }

    @Override
    public void newStudentNotification(PaymentStatusChangedNotification paymentStatusChangedNotification, String studentEmail){
        PaymentStatusChangedNotification saved = paymentStatusChangedNotificationRepository.save(paymentStatusChangedNotification);
        pushStudentNotification(saved, studentEmail);
    }

    @Override
    public void getStudentNotifications(String email){
        List<PaymentStatusChangedNotification> studentNotifications = paymentStatusChangedNotificationRepository.findAllByStudentEmail(email);
        studentNotifications.forEach(n -> {
            pushStudentNotification(n, email);
        });
    }

    @Override
    public boolean toggleStudentNotificationSeen(Long id){
        return paymentStatusChangedNotificationRepository.findById(id)
                .map(notification -> {
                    notification.setSeen(!notification.isSeen());
                    paymentStatusChangedNotificationRepository.save(notification);
                    return !notification.isSeen();
                })
                .orElse(false);
    }

    @Override
    public void deleteStudentNotification(Long id){
        paymentStatusChangedNotificationRepository.findById(id)
                .map(notification -> {
                    notification.setDeleted(true);
                    paymentStatusChangedNotificationRepository.save(notification);
                    return null;
                });
    }

    @Override
    public void studentNotificationSeen(Long id){
        paymentStatusChangedNotificationRepository.findById(id)
                .map(notification -> {
                    setStudentNotificationSeen(notification);
                    return null;
                });
    }

    @Override
    public UUID getPaymentIDAndMarkAsRead(Long id){
        return paymentStatusChangedNotificationRepository.findById(id)
                .map(notification -> {
                    setStudentNotificationSeen(notification);
                    return notification.getPaymentId();
                })
                .orElse(null);
    }

    private void setStudentNotificationSeen(PaymentStatusChangedNotification notification){
        notification.setSeen(true);
        paymentStatusChangedNotificationRepository.save(notification);
    }

    private void pushStudentNotification(PaymentStatusChangedNotification notification, String studentEmail) {
        PaymentStatusChangedNotificationDTO notificationDTO = modelMapper.map(notification, PaymentStatusChangedNotificationDTO.class);
        String destination = "/notifications/" + studentEmail + "/payment-status-changed";

        webSocketService.sendToSpecificUser(destination, notificationDTO);
    }

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
