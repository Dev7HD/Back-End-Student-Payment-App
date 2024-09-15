package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InfoPaymentDTO {
    private UUID id;
    private double amount;
    private Date date;
    private PaymentType type;
    private PaymentStatus status;
    private String receipt;
    private String studentCode;
    private String addedBy;
}
