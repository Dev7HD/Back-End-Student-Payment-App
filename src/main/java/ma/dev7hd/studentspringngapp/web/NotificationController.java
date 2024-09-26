package ma.dev7hd.studentspringngapp.web;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.NotificationDTO;
import ma.dev7hd.studentspringngapp.services.notification.INotificationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final INotificationService iNotificationService;

    @PostMapping("/delete")
    public void deleteNotification(@NotNull @RequestParam(name = "id") Long notificationId) throws ChangeSetPersister.NotFoundException {
        iNotificationService.deleteAdminNotification(notificationId);
    }

    @PostMapping("/mark-all-as-read")
    public void markAllAsRead(){
        iNotificationService.markAllAdminNotificationsAsRead();
    }

    @PatchMapping("/toggle-seen")
    public boolean toggleSeen(Long id){
        return iNotificationService.toggleAdminNotificationSeen(id);
    }

    @GetMapping("/pageable")
    public Page<NotificationDTO> pageableNotifications(@RequestParam(required = false) Boolean seen,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) throws ChangeSetPersister.NotFoundException {
        return iNotificationService.pageableAdminNotifications(seen, page, size);
    }

    @DeleteMapping("/delete-list")
    public void deleteNotifications(@RequestBody List<Long> notificationIds) throws ChangeSetPersister.NotFoundException {
        iNotificationService.deleteAdminNotifications(notificationIds);
    }

    @PatchMapping("/mark-as-read-list")
    public void markNotificationsAsRead(@RequestBody List<Long> notificationIds) {
        iNotificationService.markAdminNotificationsAsRead(notificationIds);
    }

    @PatchMapping("/toggle-seen-list")
    public void toggleNotifications(@RequestBody List<Long> notificationIds){
        iNotificationService.toggleAdminNotifications(notificationIds);
    }
}
