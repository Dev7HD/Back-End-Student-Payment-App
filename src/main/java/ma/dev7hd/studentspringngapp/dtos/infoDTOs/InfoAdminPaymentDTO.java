package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class InfoAdminPaymentDTO {
    private UUID id;
    private double amount;
    private LocalDate date;
    private PaymentType type;
    private PaymentStatus status;
    private String receipt;
    private String addedBy;
    private InfosStudentDTO studentDTO;
}
