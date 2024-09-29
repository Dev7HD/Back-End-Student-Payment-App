package ma.dev7hd.studentspringngapp.entities.notifications.students;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusChangedNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;
    private UUID paymentId;
    private String studentEmail;
    private String message;
    private Date registerDate;
    private boolean seen;
    private boolean deleted;
}
