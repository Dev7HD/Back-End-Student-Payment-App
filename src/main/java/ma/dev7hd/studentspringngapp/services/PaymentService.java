package ma.dev7hd.studentspringngapp.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoStatusChangesDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoPaymentDTO;
import ma.dev7hd.studentspringngapp.entities.Payment;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import ma.dev7hd.studentspringngapp.metier.IMetier;
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
    private IMetier iMetier;

    @Override
    public List<InfoPaymentDTO> getAllPayments() {
        return iMetier.findAllPayments();
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        return iMetier.getPaymentsByStatus(status);
    }

    @Override
    public ResponseEntity<Payment> newPayment(NewPaymentDTO newPaymentDTO,
                                              MultipartFile file) throws IOException {
        return iMetier.saveNewPayment(newPaymentDTO, file);
    }

    @Override
    public ResponseEntity<InfoPaymentDTO> paymentStatusUpdate(UUID id, PaymentStatus newStatus) {
        return iMetier.updatePaymentStatus(id, newStatus);
    }

    @Override
    public byte[] getPaymentFile(UUID paymentId) throws IOException {
        return iMetier.getReceipt(paymentId);
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByType(PaymentType type) {
        return iMetier.getPaymentsByType(type);
    }

    @Override
    public ResponseEntity<InfoPaymentDTO> getPaymentById(UUID paymentId) {
        InfoPaymentDTO payment = iMetier.getPaymentById(paymentId);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(payment);
        }
    }

    @Override
    public ResponseEntity<List<InfoPaymentDTO>> getStudentPayments(String studentCode) {
        return iMetier.getStudentPayments(studentCode);
    }

    @Override
    public List<InfoStatusChangesDTO> getPaymentStatusChanges(){
        return iMetier.getChanges();
    }

}