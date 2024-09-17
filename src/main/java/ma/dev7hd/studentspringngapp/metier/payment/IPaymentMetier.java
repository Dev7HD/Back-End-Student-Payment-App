package ma.dev7hd.studentspringngapp.metier.payment;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.*;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.enumirat.Months;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IPaymentMetier {
    ResponseEntity<InfoSavedPayment> saveNewPayment(NewPaymentDTO newPaymentDTO,
                                                    @NotNull MultipartFile file) throws IOException;

    ResponseEntity<InfoPaymentDTO> updatePaymentStatus(UUID paymentId, PaymentStatus newStatus);

    byte[] getReceipt(UUID paymentId) throws IOException;

    List<InfoPaymentDTO> findAllPayments();

    List<InfoPaymentDTO> getPaymentsByStatus(PaymentStatus status);

    List<InfoPaymentDTO> getPaymentsByType(PaymentType type);

    InfoPaymentDTO getPaymentById(UUID paymentId);

    ResponseEntity<List<InfoPaymentDTO>> getStudentPayments(String code);

    List<InfoStatusChangesDTO> getChanges();

    Page<InfoAdminPaymentDTO> getPaymentsByCriteriaAsAdmin(String email, String code, Double min, Double max, PaymentStatus status, PaymentType type, int page, int size);

    Page<InfoStudentPaymentDTO> getPaymentsByCriteriaAsStudent(Double min, Double max, PaymentStatus status, PaymentType type, int page, int size);

    ResponseEntity<Map<Months, Long>> getPaymentsByMonth(Integer month);

}
