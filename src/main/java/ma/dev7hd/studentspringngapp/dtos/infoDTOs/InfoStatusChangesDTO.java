package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;

import java.util.UUID;

@Getter @Setter
public class InfoStatusChangesDTO {
    private UUID id;
    private String adminEmail;
    private UUID paymentId;
    private PaymentStatus newStatus;
    private PaymentStatus oldStatus;
}
