package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;

import java.util.Date;
import java.util.UUID;

@Getter @Setter
public class PaymentNotificationDTO {
    private UUID id;
    private Date registerDate;
    private double amount;
    private PaymentType type;
    private String addedBy;
    private String message;
    private boolean seen;
}
