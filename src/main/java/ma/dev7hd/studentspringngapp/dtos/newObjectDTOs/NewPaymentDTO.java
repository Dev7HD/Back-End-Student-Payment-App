package ma.dev7hd.studentspringngapp.dtos.newObjectDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;

import java.util.Date;

@Getter @Setter @AllArgsConstructor
public class NewPaymentDTO {
    private String studentCode;
    private double amount;
    private PaymentType paymentType;
    private Date date;
}
