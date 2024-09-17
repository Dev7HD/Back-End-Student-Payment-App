package ma.dev7hd.studentspringngapp.web;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.metier.notification.INotificationMetier;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final INotificationMetier notificationMetier;

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

}
