package ma.dev7hd.studentspringngapp.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoAdminPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoStatusChangesDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoStudentPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoPaymentDTO;
import ma.dev7hd.studentspringngapp.entities.Payment;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import ma.dev7hd.studentspringngapp.metier.payment.IPaymentMetier;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class PaymentService implements IPaymentService {
    private final IPaymentMetier paymentMetier;

    @Override
    public List<InfoPaymentDTO> getAllPayments() {
        return paymentMetier.findAllPayments();
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        return paymentMetier.getPaymentsByStatus(status);
    }

    @Override
    public ResponseEntity<Payment> newPayment(NewPaymentDTO newPaymentDTO,
                                              MultipartFile file) throws IOException {
        return paymentMetier.saveNewPayment(newPaymentDTO, file);
    }

    @Override
    public ResponseEntity<InfoPaymentDTO> paymentStatusUpdate(UUID id, PaymentStatus newStatus) {
        return paymentMetier.updatePaymentStatus(id, newStatus);
    }

    @Override
    public byte[] getPaymentFile(UUID paymentId) throws IOException {
        return paymentMetier.getReceipt(paymentId);
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByType(PaymentType type) {
        return paymentMetier.getPaymentsByType(type);
    }

    @Override
    public ResponseEntity<InfoPaymentDTO> getPaymentById(UUID paymentId) {
        InfoPaymentDTO payment = paymentMetier.getPaymentById(paymentId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(payment);
        }
    }

    @Override
    public ResponseEntity<List<InfoPaymentDTO>> getStudentPayments(String studentCode) {
        return paymentMetier.getStudentPayments(studentCode);
    }

    @Override
    public List<InfoStatusChangesDTO> getPaymentStatusChanges(){
        return paymentMetier.getChanges();
    }

    @Override
    public Page<InfoAdminPaymentDTO> getPaymentsByCriteriaAsAdmin(String email, String code, Double min, Double max, PaymentStatus status, PaymentType type, int page, int size) {
        return paymentMetier.getPaymentsByCriteriaAsAdmin(email, code, min, max, status, type, page, size);
    }

    @Override
    public Page<InfoStudentPaymentDTO> getPaymentsByCriteriaAsStudent(Double min, Double max, PaymentStatus status, PaymentType type, int page, int size) {
        return paymentMetier.getPaymentsByCriteriaAsStudent(min, max, status, type, page, size);
    }

}