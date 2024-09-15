package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InfoStudentPaymentDTO {
    private double amount;
    private Date date;
    private PaymentType type;
    private PaymentStatus status;
    private String receipt;
}
