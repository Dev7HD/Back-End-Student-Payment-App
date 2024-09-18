package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NewPaymentNotificationDTO extends NotificationDTO {
    private UUID paymentId;
}
