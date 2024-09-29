package ma.dev7hd.studentspringngapp.services.payment;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.*;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.UpdatePaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IPaymentService {
    ResponseEntity<InfoSavedPayment> saveNewPayment(NewPaymentDTO newPaymentDTO,
                                                    @NotNull MultipartFile file) throws IOException;

    ResponseEntity<InfoPaymentDTO> updatePaymentStatus(UUID paymentId, PaymentStatus newStatus);

    byte[] getReceipt(UUID paymentId) throws IOException;

    List<InfoPaymentDTO> getAllPayments();

    List<InfoPaymentDTO> getPaymentsByStatus(PaymentStatus status);

    List<InfoPaymentDTO> getPaymentsByType(PaymentType type);

    ResponseEntity<InfoPaymentDTO> getPaymentById(UUID paymentId);

    ResponseEntity<List<InfoPaymentDTO>> getStudentPayments(String code);

    Page<InfoStatusChangesDTO> getPaymentsStatusChangers(String email, UUID paymentId, PaymentStatus newStatus, PaymentStatus oldStatus, int page, int size);

    Page<InfoAdminPaymentDTO> getPaymentsByCriteriaAsAdmin(String email, String code, Double min, Double max, PaymentStatus status, PaymentType type, int page, int size);

    Page<InfoStudentPaymentDTO> getPaymentsByCriteriaAsStudent(Double min, Double max, PaymentStatus status, PaymentType type, int page, int size);

    void updatePaymentsStatus(UpdatePaymentStatus updatePaymentStatus);

    ResponseEntity<InfoStudentPaymentDTO> getPaymentAsStudent(Long notificationId);
}
