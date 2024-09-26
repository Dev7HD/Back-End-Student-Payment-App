package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter @Setter
public class PaymentStatusChangedNotificationDTO {
    private Long id;
    private UUID paymentId;
    private String message;
    private Date date;
    private boolean seen;
}
