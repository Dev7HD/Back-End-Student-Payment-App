package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;

import java.util.Date;

@Getter @Setter
public class InfoSavedPayment {
    private double amount;
    private Date date;
    private PaymentType type;
    private PaymentStatus status;
    private String receipt;
}
