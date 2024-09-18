package ma.dev7hd.studentspringngapp.web;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.NotificationDTO;
import ma.dev7hd.studentspringngapp.services.notification.INotificationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final INotificationService notificationMetier;

    @PostMapping("/delete")
    public void deleteNotification(@NotNull @RequestParam(name = "id") Long notificationId) throws ChangeSetPersister.NotFoundException {
        notificationMetier.deleteNotification(notificationId);
    }

    @PostMapping("/mark-all-as-read")
    public void markAllAsRead(){
        notificationMetier.markAllAsRead();
    }

    @PatchMapping("/toggle-seen")
    public boolean toggleSeen(Long id){
        return notificationMetier.toggleSeen(id);
    }

    @GetMapping("/pageable")
    public Page<NotificationDTO> pageableNotifications(@RequestParam(required = false) Boolean seen,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) throws ChangeSetPersister.NotFoundException {
        return notificationMetier.pageableNotifications(seen, page, size);
    }

}
