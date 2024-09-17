package ma.dev7hd.studentspringngapp.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.UUID;

@Entity
@DiscriminatorValue("NEW_PAYMENT")
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class NewPaymentNotification extends Notification {
    private UUID paymentId;
}