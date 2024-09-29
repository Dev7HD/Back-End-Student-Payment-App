package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter @Setter
public class PaymentStatusChangedNotificationDTO {
    private Long notificationId;
    private UUID paymentId;
    private String message;
    private Date registerDate;
    private boolean seen;
}
