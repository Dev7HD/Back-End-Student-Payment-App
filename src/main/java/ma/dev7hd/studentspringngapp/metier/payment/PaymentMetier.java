package ma.dev7hd.studentspringngapp.metier.payment;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.*;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.entities.*;
import ma.dev7hd.studentspringngapp.enumirat.Months;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import ma.dev7hd.studentspringngapp.repositories.*;
import ma.dev7hd.studentspringngapp.websoket.config.WebSocketService;
import org.jetbrains.annotations.NotNull;
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class PaymentMetier implements IPaymentMetier {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusChangeRepository paymentStatusChangeRepository;
    private final ModelMapper modelMapper;
    private final WebSocketService webSocketService;

    private static final Path PAYMENTS_FOLDER_PATH = Paths.get(System.getProperty("user.home"), "data", "payments");

    @Override
    public ResponseEntity<InfoSavedPayment> saveNewPayment(NewPaymentDTO newPaymentDTO,
                                                           @NotNull MultipartFile file) throws IOException {
        if (!Objects.equals(file.getContentType(), MediaType.APPLICATION_PDF_VALUE)) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> optionalLoggedInUser = getCurrentUser();

        Optional<Student> optionalStudent = studentRepository.findStudentByCode(newPaymentDTO.getStudentCode());

        if (optionalStudent.isPresent() && optionalLoggedInUser.isPresent()) {
            Student student = optionalStudent.get();
            User user = optionalLoggedInUser.get();
            Path filePath = storePaymentReceipt(file);
            if (filePath != null) {
                Payment payment = buildPayment(newPaymentDTO, student, user, filePath.toString());
                Payment saved = paymentRepository.save(payment);
                InfoSavedPayment savedPaymentDTO = modelMapper.map(payment, InfoSavedPayment.class);

                // Send new payment notification
                sendPaymentNotification(saved, user);
                return ResponseEntity.ok(savedPaymentDTO);
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
        paymentsSeen(payments);
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        paymentsSeen(payments);
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByType(PaymentType type) {
        List<Payment> payments = paymentRepository.findByType(type);
        paymentsSeen(payments);
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public InfoPaymentDTO getPaymentById(UUID paymentId) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            paymentsSeen(payment);
            return convertPaymentToDto(payment);
        }
        return null;
    }

    @Override
    public ResponseEntity<List<InfoPaymentDTO>> getStudentPayments(String code) {
        Optional<Student> optionalStudent = studentRepository.findStudentByCode(code);
        if (optionalStudent.isPresent()) {
            List<Payment> payments = paymentRepository.findByStudentCode(code);
            paymentsSeen(payments);
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
    public Page<InfoAdminPaymentDTO> getPaymentsByCriteriaAsAdmin(String email, String code, Double min, Double max, PaymentStatus status, PaymentType type, int page, int size){
        Page<Payment> payments = paymentRepository.findByFilters(code, email, min, max, type, status, PageRequest.of(page, size));
        if (payments.getTotalElements() > 0){
            paymentsSeen(payments.getContent());
        }
        return convertPageablePaymentToDTOAsAdmin(payments);
    }

    @Override
    public Page<InfoStudentPaymentDTO> getPaymentsByCriteriaAsStudent(Double min, Double max, PaymentStatus status, PaymentType type, int page, int size){
        String currentStudentEmail = getCurrentUserEmail();
        Page<Payment> payments = paymentRepository.findByFilters("", currentStudentEmail, min, max, type, status, PageRequest.of(page, size));
        return convertPageablePaymentToDTOAsStudent(payments);
    }

    @Override
    public void onLoginPaymentNotifications(){
        List<Payment> paymentList = paymentRepository.findAllBySeen(false);
        paymentList.forEach(payment -> sendPaymentNotification(payment,payment.getAddedBy()));
    }

    @Override
    public void markAllAsRead(){
        List<Payment> paymentList = paymentRepository.findAllBySeen(false);
        paymentList.forEach(payment -> {
           payment.setSeen(true);
           paymentRepository.save(payment);
        });
    }

    // Private methods

    private void sendPaymentNotification(Payment payment, User user){
        String message = "New Payment made by " + user.getLastName() + " " + user.getFirstName() + " need to be reviewed.";
        PaymentNotificationDTO paymentNotificationDTO = modelMapper.map(payment, PaymentNotificationDTO.class);
        paymentNotificationDTO.setMessage(message);
        paymentNotificationDTO.setAddedBy(payment.getAddedBy().getEmail());
        webSocketService.sendToSpecificUser("/notifications/new-payment", paymentNotificationDTO);
    }

    private Page<InfoAdminPaymentDTO> convertPageablePaymentToDTOAsAdmin(Page<Payment> payments) {
        return payments.map(payment -> {
            InfoAdminPaymentDTO paymentDTO = modelMapper.map(payment, InfoAdminPaymentDTO.class);
            InfosStudentDTO studentDTO = modelMapper.map(payment.getStudent(), InfosStudentDTO.class);
            paymentDTO.setStudentDTO(studentDTO);
            paymentDTO.setAddedBy(payment.getAddedBy().getEmail());
            return paymentDTO;
        });
    }

    private Page<InfoStudentPaymentDTO> convertPageablePaymentToDTOAsStudent(Page<Payment> payments) {
        return payments.map(payment -> modelMapper.map(payment, InfoStudentPaymentDTO.class));
    }


    private InfoStatusChangesDTO convertChanges(PaymentStatusChange paymentStatusChange) {
        InfoStatusChangesDTO changes = modelMapper.map(paymentStatusChange, InfoStatusChangesDTO.class);
        changes.setAdminEmail(paymentStatusChange.getAdmin().getEmail());
        changes.setPaymentId(paymentStatusChange.getPayment().getId());
        return changes;
    }

    private void paymentsSeen(List<Payment> payments) {
        payments.forEach(payment -> {
            if(!payment.isSeen()){
                payment.setSeen(true);
                paymentRepository.save(payment);
            }
        });
    }

    private void paymentsSeen(Payment payment) {
        if(!payment.isSeen()){
            payment.setSeen(true);
            paymentRepository.save(payment);
        }
    }

    private InfoPaymentDTO convertPaymentToDto(Payment payment) {
        InfoPaymentDTO paymentsDTO = modelMapper.map(payment, InfoPaymentDTO.class);

        paymentsDTO.setStudentCode(payment.getStudent().getCode());
        paymentsDTO.setAddedBy(payment.getAddedBy().getEmail());

        return paymentsDTO;
    }

    private ResponseEntity<InfoPaymentDTO> processPaymentStatusUpdate(Payment payment, PaymentStatus newStatus) {
        String currentUserEmail = getCurrentUserEmail();
        Optional<Admin> optionalAdmin = adminRepository.findById(currentUserEmail);
        PaymentStatus oldStatus = payment.getStatus();
        if (optionalAdmin.isPresent()) {
            payment.setStatus(newStatus);
            if(!payment.isSeen()){
                payment.setSeen(true);
            }

            PaymentStatusChange changes = PaymentStatusChange.builder()
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .admin(optionalAdmin.get())
                    .payment(payment)
                    .changeDate(new Date())
                    .build();

            paymentStatusChangeRepository.save(changes);
            paymentRepository.save(payment);

            InfoPaymentDTO infoPaymentDTO = modelMapper.map(payment, InfoPaymentDTO.class);
            infoPaymentDTO.setAddedBy(payment.getAddedBy().getEmail());
            
            return ResponseEntity.ok(infoPaymentDTO);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private Payment buildPayment(NewPaymentDTO newPaymentDTO, Student student, User user, String receiptPath) {
        File file = new File(receiptPath);
        String fileUri = file.toURI().toString();
        return Payment.builder()
                .amount(newPaymentDTO.getAmount())
                .student(student)
                .type(newPaymentDTO.getPaymentType())
                .date(newPaymentDTO.getDate())
                .status(PaymentStatus.CREATED)
                .receipt(fileUri)
                .addedBy(user)
                .seen(false)
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

    @Override
    public ResponseEntity<Map<Months, Long>> getPaymentsByMonth(Integer month) {
        if(month != null && (month > 12 || month < 1)) {
            return ResponseEntity.badRequest().build();
        }

        Map<Months, Long> countByMonth = new EnumMap<>(Months.class);
        List<Long[]> counted = paymentRepository.countAllPaymentsGroupByDateAndOptionalMonth(month);
        if(month == null){
            int i = 0;
            for(Months months : Months.values()) {
                countByMonth.put(months, counted.get(i)[1]);
                System.out.println("Month: " + months + ", Count: " + countByMonth.get(months));
                i++;
            }
        } else {
            countByMonth.put(Months.values()[month - 1], counted.get(0)[1]);
        }

        return ResponseEntity.ok(countByMonth);

    }
}
