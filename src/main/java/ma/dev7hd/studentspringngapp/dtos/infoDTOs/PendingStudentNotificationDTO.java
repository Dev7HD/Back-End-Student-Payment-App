package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PendingStudentNotificationDTO extends NotificationDTO {
    private String email;
}
