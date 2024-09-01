package ma.dev7hd.studentspringngapp.metier.payment;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoStatusChangesDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.entities.*;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import ma.dev7hd.studentspringngapp.repositories.PaymentRepository;
import ma.dev7hd.studentspringngapp.repositories.PaymentStatusChangeRepository;
import ma.dev7hd.studentspringngapp.repositories.StudentRepository;
import ma.dev7hd.studentspringngapp.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class PaymentMetier implements IPaymentMetier {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusChangeRepository paymentStatusChangeRepository;
    private final ModelMapper modelMapper;


    private static final Path PAYMENTS_FOLDER_PATH = Paths.get(System.getProperty("user.home"), "data", "payments");

    @Override
    public ResponseEntity<Payment> saveNewPayment(NewPaymentDTO newPaymentDTO,
                                                  @org.jetbrains.annotations.NotNull MultipartFile file) throws IOException {
        if (!Objects.equals(file.getContentType(), MediaType.APPLICATION_PDF_VALUE)) {
            return ResponseEntity.badRequest().build();
        }

        // User who want to save the new payment
        Optional<User> optionalLoggedInUser = getCurrentUser();

        // Student who made the payment
        Optional<Student> optionalStudent = studentRepository.findStudentByCode(newPaymentDTO.getStudentCode());

        if (optionalStudent.isPresent() && optionalLoggedInUser.isPresent()) {
            Student student = optionalStudent.get();
            User user = optionalLoggedInUser.get();
            Path filePath = storePaymentReceipt(file);
            if (filePath != null) {
                Payment payment = buildPayment(newPaymentDTO, student, user, filePath.toString());
                paymentRepository.save(payment);
                return ResponseEntity.ok(payment);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<InfoPaymentDTO> updatePaymentStatus(UUID paymentId, PaymentStatus newStatus) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);

        if (optionalPayment.isPresent()) {
            return processPaymentStatusUpdate(optionalPayment.get(),newStatus);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public byte[] getReceipt(UUID paymentId) throws IOException {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isPresent()) {
            return Files.readAllBytes(Path.of(URI.create(optionalPayment.get().getReceipt())));
        }
        return null;
    }

    @Override
    public List<InfoPaymentDTO> findAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByType(PaymentType type) {
        List<Payment> payments = paymentRepository.findByType(type);
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public InfoPaymentDTO getPaymentById(UUID paymentId) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            return convertPaymentToDto(payment);
        }
        return null;
    }

    @Override
    public ResponseEntity<List<InfoPaymentDTO>> getStudentPayments(String code) {
        Optional<Student> optionalStudent = studentRepository.findStudentByCode(code);
        if (optionalStudent.isPresent()) {
            List<Payment> payments = paymentRepository.findByStudentCode(code);
            List<InfoPaymentDTO> paymentDTOS = payments.stream()
                    .map(this::convertPaymentToDto)
                    .toList();
            return ResponseEntity.ok(paymentDTOS);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public List<InfoStatusChangesDTO> getChanges(){
        List<PaymentStatusChange> changes = paymentStatusChangeRepository.findAll();
        return changes.stream().map(this::convertChanges).toList();
    }

    @Override
    public Page<InfoPaymentDTO> getPaymentsByCriteria(String code, Double min, Double max, PaymentStatus status, PaymentType type, int page, int size){
        Page<Payment> payments = paymentRepository.findByFilters(code, min, max, type, status, PageRequest.of(page, size));
        return convertPageablePaymentToDTO(payments);
    }

    // Private methods

    private Page<InfoPaymentDTO> convertPageablePaymentToDTO(Page<Payment> payments){
        return payments.map(payment -> modelMapper.map(payment, InfoPaymentDTO.class));
    }

    private InfoStatusChangesDTO convertChanges(PaymentStatusChange paymentStatusChange) {
        InfoStatusChangesDTO changes = modelMapper.map(paymentStatusChange, InfoStatusChangesDTO.class);
        changes.setAdminEmail(paymentStatusChange.getAdmin().getEmail());
        changes.setPaymentId(paymentStatusChange.getPayment().getId());
        return changes;
    }

    private InfoPaymentDTO convertPaymentToDto(Payment payment) {
        InfoPaymentDTO paymentsDTO = modelMapper.map(payment, InfoPaymentDTO.class);

        paymentsDTO.setStudentCode(payment.getStudent().getCode());
        paymentsDTO.setAddedBy(payment.getAddedBy().getEmail());

        return paymentsDTO;
    }

    private ResponseEntity<InfoPaymentDTO> processPaymentStatusUpdate(Payment payment, PaymentStatus newStatus) {
        User user = getCurrentUser().orElse(null);

        if (user instanceof Admin admin) {
            PaymentStatus oldStatus = payment.getStatus();
            payment.setStatus(newStatus);

            PaymentStatusChange changes = PaymentStatusChange.builder()
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .admin(admin)
                    .payment(payment)
                    .changeDate(LocalDateTime.now())
                    .build();

            paymentStatusChangeRepository.save(changes);
            paymentRepository.save(payment);

            InfoPaymentDTO infoPaymentDTO = modelMapper.map(payment, InfoPaymentDTO.class);
            return ResponseEntity.ok(infoPaymentDTO);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private Payment buildPayment(NewPaymentDTO newPaymentDTO, Student student, User user, String receiptUri) {
        return Payment.builder()
                .amount(newPaymentDTO.getAmount())
                .student(student)
                .type(newPaymentDTO.getPaymentType())
                .date(newPaymentDTO.getDate())
                .status(PaymentStatus.CREATED)
                .receipt(receiptUri)
                .addedBy(user)
                .build();
    }

    private Path storePaymentReceipt(MultipartFile file) throws IOException {
        if (!Files.exists(PAYMENTS_FOLDER_PATH)) {
            Files.createDirectories(PAYMENTS_FOLDER_PATH);
        }
        String fileName = UUID.randomUUID().toString();
        Path filePath = PAYMENTS_FOLDER_PATH.resolve(fileName + ".pdf");

        try {
            Files.copy(file.getInputStream(), filePath);
        } catch (IOException e) {
            return null;
        }
        return filePath;
    }

    private Optional<User> getCurrentUser() {
        String userEmail = getCurrentUserEmail();
        return userRepository.findByEmail(userEmail);
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
