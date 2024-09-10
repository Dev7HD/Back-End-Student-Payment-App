package ma.dev7hd.studentspringngapp.services;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.*;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.enumirat.Months;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IPaymentService {
    List<InfoPaymentDTO> getAllPayments();

    List<InfoPaymentDTO> getPaymentsByStatus(PaymentStatus status);

    ResponseEntity<InfoSavedPayment> newPayment(NewPaymentDTO newPaymentDTO,
                                                MultipartFile file) throws IOException;

    ResponseEntity<InfoPaymentDTO> paymentStatusUpdate(UUID id, PaymentStatus status);

    byte[] getPaymentFile(UUID paymentId) throws IOException;

    List<InfoPaymentDTO> getPaymentsByType(PaymentType type);

    ResponseEntity<InfoPaymentDTO> getPaymentById(UUID paymentId);

    ResponseEntity<List<InfoPaymentDTO>> getStudentPayments(String studentCode);

    List<InfoStatusChangesDTO> getPaymentStatusChanges();

    Page<InfoAdminPaymentDTO> getPaymentsByCriteriaAsAdmin(String email, String code, Double min, Double max, PaymentStatus status, PaymentType type, int page, int size);

    Page<InfoStudentPaymentDTO> getPaymentsByCriteriaAsStudent(Double min, Double max, PaymentStatus status, PaymentType type, int page, int size);

    ResponseEntity<Map<Months, Long>> getPaymentsByMonth(Integer month);
}
