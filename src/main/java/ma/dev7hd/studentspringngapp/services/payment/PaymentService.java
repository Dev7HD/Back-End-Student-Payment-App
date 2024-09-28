package ma.dev7hd.studentspringngapp.services.payment;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.*;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.UpdatePaymentStatus;
import ma.dev7hd.studentspringngapp.entities.notifications.admins.NewPaymentNotification;
import ma.dev7hd.studentspringngapp.entities.notifications.students.PaymentStatusChangedNotification;
import ma.dev7hd.studentspringngapp.entities.payments.Payment;
import ma.dev7hd.studentspringngapp.entities.payments.PaymentStatusChange;
import ma.dev7hd.studentspringngapp.entities.users.*;
import ma.dev7hd.studentspringngapp.enumirat.Months;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import ma.dev7hd.studentspringngapp.repositories.payments.PaymentRepository;
import ma.dev7hd.studentspringngapp.repositories.payments.PaymentStatusChangeRepository;
import ma.dev7hd.studentspringngapp.repositories.users.AdminRepository;
import ma.dev7hd.studentspringngapp.repositories.users.StudentRepository;
import ma.dev7hd.studentspringngapp.repositories.users.UserRepository;
import ma.dev7hd.studentspringngapp.services.notification.INotificationService;
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
public class PaymentService implements IPaymentService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusChangeRepository paymentStatusChangeRepository;
    private final ModelMapper modelMapper;
    private final INotificationService notificationService;

    private static final Path PAYMENTS_FOLDER_PATH = Paths.get(System.getProperty("user.home"), "data", "payments");

    @Override
    @Transactional
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
    @Transactional
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
    public List<InfoPaymentDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        paymentNotificationSeen(payments);
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);
        paymentNotificationSeen(payments);
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InfoPaymentDTO> getPaymentsByType(PaymentType type) {
        List<Payment> payments = paymentRepository.findByType(type);
        paymentNotificationSeen(payments);
        return payments.stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<InfoPaymentDTO> getPaymentById(UUID paymentId) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            notificationService.adminNotificationSeen(payment.getId(), null);
            return ResponseEntity.ok(convertPaymentToDto(payment));
        }
        return null;
    }

    @Override
    public ResponseEntity<List<InfoPaymentDTO>> getStudentPayments(String code) {
        Optional<Student> optionalStudent = studentRepository.findStudentByCode(code);
        if (optionalStudent.isPresent()) {
            List<Payment> payments = paymentRepository.findByStudentCode(code);
            paymentNotificationSeen(payments);
            List<InfoPaymentDTO> paymentDTOS = payments.stream()
                    .map(this::convertPaymentToDto)
                    .toList();
            return ResponseEntity.ok(paymentDTOS);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public Page<InfoStatusChangesDTO> getPaymentsStatusChangers(String email, UUID paymentId, PaymentStatus newStatus, PaymentStatus oldStatus, int page, int size){
        Admin admin = email != null ? adminRepository.findById(email).orElse(null) : null;
        Payment payment = paymentId != null ? paymentRepository.findById(paymentId).orElse(null) : null;
        Page<PaymentStatusChange> statusChanges = paymentStatusChangeRepository.findAll(admin, payment, newStatus, oldStatus, PageRequest.of(page, size));
        return convertChanges(statusChanges);
    }

    @Override
    public Page<InfoAdminPaymentDTO> getPaymentsByCriteriaAsAdmin(String email, String code, Double min, Double max, PaymentStatus status, PaymentType type, int page, int size){
        Page<Payment> payments = paymentRepository.findByFilters(code, email, min, max, type, status, PageRequest.of(page, size));
        if (payments.getTotalElements() > 0){
            paymentNotificationSeen(payments.getContent());
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
    public ResponseEntity<Map<Months, Long>> getPaymentsByMonth(Integer month) {
        if(month != null && (month > 12 || month < 1)) {
            return ResponseEntity.badRequest().build();
        }

        Map<Months, Long> countByMonth = new EnumMap<>(Months.class);
        if(month == null){
            List<Long[]> counted = paymentRepository.countAllPaymentsGroupByDateAndOptionalMonth(month);
            int i = 0;
            for(Months months : Months.values()) {
                countByMonth.put(months, counted.get(i)[1]);
                i++;
            }
        } else {
            Long[] counted = paymentRepository.countPaymentsByMonth(month);
            countByMonth.put(Months.values()[month - 1], counted[0]);
        }
        return ResponseEntity.ok(countByMonth);
    }

    @Override
    public void updatePaymentsStatus(UpdatePaymentStatus updatePaymentStatus){
        Optional<Admin> currentAdmin = getCurrentAdmin();
        List<Payment> allPayments = paymentRepository.findAllById(updatePaymentStatus.getIds());
        if (currentAdmin.isPresent() && !allPayments.isEmpty()){
            Admin admin = currentAdmin.get();
            List<Payment> payments = allPayments.stream()
                    .filter(payment -> payment.getStatus() != updatePaymentStatus.getNewPaymentStatus())
                    .toList();
            List<PaymentStatusChange> paymentStatusChanges = new ArrayList<>();
            payments.forEach(payment -> {
                PaymentStatus oldStatus = payment.getStatus();
                PaymentStatusChange paymentStatusChange = PaymentStatusChange.builder()
                        .payment(payment)
                        .admin(admin)
                        .changeDate(new Date())
                        .newStatus(updatePaymentStatus.getNewPaymentStatus())
                        .oldStatus(oldStatus)
                        .build();
                paymentStatusChanges.add(paymentStatusChange);
                payment.setStatus(updatePaymentStatus.getNewPaymentStatus());
            });
            paymentRepository.saveAll(payments);
            paymentStatusChangeRepository.saveAll(paymentStatusChanges);
        }
    }

    @Override
    public ResponseEntity<InfoStudentPaymentDTO> getPaymentAsStudent(Long notificationId){
        UUID paymentId = notificationService.getPaymentIDAndMarkAsRead(notificationId);
        Optional<Student> optionalStudent = getCurrentStudent();
        InfoPaymentDTO paymentDTO = getPaymentById(paymentId).getBody();
        if(optionalStudent.isPresent() && paymentDTO != null){
            InfoStudentPaymentDTO studentPaymentDTO = modelMapper.map(paymentDTO, InfoStudentPaymentDTO.class);
            return ResponseEntity.ok(studentPaymentDTO);
        }
        return ResponseEntity.badRequest().build();
    }

    // Private methods

    private void sendPaymentNotification(Payment payment, User user){
        String message = user.getLastName() + " " + user.getFirstName() + " made a new payment of " + payment.getAmount() + " DHs, using " + payment.getType() + " need to be reviewed.";

        NewPaymentNotification notification = new NewPaymentNotification();
        notification.setMessage(message);
        notification.setSeen(false);
        notification.setRegisterDate(new Date());
        notification.setPaymentId(payment.getId());

        notificationService.newAdminNotification(notification);
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


    private Page<InfoStatusChangesDTO> convertChanges(Page<PaymentStatusChange> paymentStatusChanges) {
        return paymentStatusChanges.map(change -> modelMapper.map(change, InfoStatusChangesDTO.class));
    }

    private InfoPaymentDTO convertPaymentToDto(Payment payment) {
        InfoPaymentDTO paymentsDTO = modelMapper.map(payment, InfoPaymentDTO.class);

        paymentsDTO.setStudentCode(payment.getStudent().getCode());
        paymentsDTO.setAddedBy(payment.getAddedBy().getEmail());

        return paymentsDTO;
    }

    private ResponseEntity<InfoPaymentDTO> processPaymentStatusUpdate(Payment payment, PaymentStatus newStatus) {
        Optional<Admin> optionalAdmin = getCurrentAdmin();
        PaymentStatus oldStatus = payment.getStatus();
        if (optionalAdmin.isPresent()) {
            payment.setStatus(newStatus);

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

            PaymentStatusChangedNotification paymentStatusChangedNotification = buildPaymentStatusChangedNotification(payment, newStatus);

            notificationService.newStudentNotification(paymentStatusChangedNotification, payment.getStudent().getEmail());
            
            return ResponseEntity.ok(infoPaymentDTO);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private static @NotNull PaymentStatusChangedNotification buildPaymentStatusChangedNotification(Payment payment, PaymentStatus newStatus) {
        String message = "Your payment was " + newStatus + ".";

        PaymentStatusChangedNotification paymentStatusChangedNotification = new PaymentStatusChangedNotification();
        paymentStatusChangedNotification.setDate(new Date());
        paymentStatusChangedNotification.setPaymentId(payment.getId());
        paymentStatusChangedNotification.setDeleted(false);
        paymentStatusChangedNotification.setSeen(false);
        paymentStatusChangedNotification.setMessage(message);
        paymentStatusChangedNotification.setStudentEmail(payment.getStudent().getEmail());
        return paymentStatusChangedNotification;
    }

    private Payment buildPayment(NewPaymentDTO newPaymentDTO, Student student, User user, String receiptPath) {
        File file = new File(receiptPath);
        String fileUri = file.toURI().toString();
        return Payment.builder()
                .amount(newPaymentDTO.getAmount())
                .student(student)
                .type(newPaymentDTO.getPaymentType())
                .date(newPaymentDTO.getDate())
                .registerDate(new Date())
                .status(PaymentStatus.CREATED)
                .receipt(fileUri)
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

    private Optional<Admin> getCurrentAdmin() {
        String userEmail = getCurrentUserEmail();
        return adminRepository.findById(userEmail);
    }

    private Optional<Student> getCurrentStudent(){
        String currentUserEmail = getCurrentUserEmail();
        return studentRepository.findById(currentUserEmail);
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void paymentNotificationSeen(List<Payment> payments){
        for(Payment payment : payments){
            notificationService.adminNotificationSeen(payment.getId(), null);
        }
    }
}
