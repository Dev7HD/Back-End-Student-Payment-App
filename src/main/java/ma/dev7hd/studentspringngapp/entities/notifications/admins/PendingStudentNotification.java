package ma.dev7hd.studentspringngapp.entities.notifications.admins;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("NEW_REGISTRATION")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingStudentNotification extends Notification {
    private String email;
}
