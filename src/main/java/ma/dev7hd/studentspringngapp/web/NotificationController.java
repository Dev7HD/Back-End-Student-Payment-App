package ma.dev7hd.studentspringngapp.web;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.NotificationDTO;
import ma.dev7hd.studentspringngapp.services.notification.INotificationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final INotificationService iNotificationService;

    @PostMapping("/delete")
    public void deleteNotification(@NotNull @RequestParam(name = "id") Long notificationId) throws ChangeSetPersister.NotFoundException {
        iNotificationService.deleteNotification(notificationId);
    }

    @PostMapping("/mark-all-as-read")
    public void markAllAsRead(){
        iNotificationService.markAllAsRead();
    }

    @PatchMapping("/toggle-seen")
    public boolean toggleSeen(Long id){
        return iNotificationService.toggleSeen(id);
    }

    @GetMapping("/pageable")
    public Page<NotificationDTO> pageableNotifications(@RequestParam(required = false) Boolean seen,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) throws ChangeSetPersister.NotFoundException {
        return iNotificationService.pageableNotifications(seen, page, size);
    }

    @DeleteMapping("/delete-list")
    public void deleteNotifications(@RequestBody Long[] notificationIds) throws ChangeSetPersister.NotFoundException {
        iNotificationService.deleteNotifications(notificationIds);
    }

    @PatchMapping("/mark-as-read")
    public void markNotificationsAsRead(@RequestBody Long[] notificationIds) {
        iNotificationService.markNotificationsAsRead(notificationIds);
    }

    @PatchMapping("/toggle-seen-list")
    public void toggleNotifications(@RequestBody Map<String, List<Long>> notificationIds){
        System.out.println(notificationIds.get("ids").size());
        iNotificationService.toggleNotifications(notificationIds.get("ids"));
    }
}
