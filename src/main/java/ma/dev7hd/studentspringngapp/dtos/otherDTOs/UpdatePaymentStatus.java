package ma.dev7hd.studentspringngapp.dtos.otherDTOs;

import lombok.Getter;
import lombok.Setter;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;

import java.util.List;
import java.util.UUID;

@Getter @Setter
public class UpdatePaymentStatus {
    private PaymentStatus newPaymentStatus;
    private List<UUID> ids;
}
