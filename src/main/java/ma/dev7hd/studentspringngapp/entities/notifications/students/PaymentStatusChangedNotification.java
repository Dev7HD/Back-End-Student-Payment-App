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
    private Long id;
    private UUID paymentId;
    private String message;
    private Date date;
    private boolean seen;
    private boolean deleted;
}
