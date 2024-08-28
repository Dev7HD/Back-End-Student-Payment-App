package ma.dev7hd.studentspringngapp.metier;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoStatusChangesDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.entities.*;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.*;
import ma.dev7hd.studentspringngapp.security.services.ISecurityService;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MetierImpl implements IMetier {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusChangeRepository paymentStatusChangeRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final ISecurityService securityService;
    private final UserTokensRepository userTokensRepository;

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
    public ResponseEntity<User> deleteUser(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(user instanceof Student) {
                paymentRepository.deleteByStudentCode(((Student) user).getCode());
            }
            userRepository.delete(user);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
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
    public List<InfosStudentDTO> getAllStudents(){
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(this::convertStudentToDto)
                .toList();
    }

    @Override
    public ResponseEntity<InfosStudentDTO> getStudentByCode(String code) {
        Optional<Student> optionalStudent = studentRepository.findStudentByCode(code);
        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            return ResponseEntity.ok(convertStudentToDto(student));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<InfosStudentDTO> getStudentById(String email) {
        Optional<Student> optionalStudent = studentRepository.findById(email);
        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            return ResponseEntity.ok(convertStudentToDto(student));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public List<InfosStudentDTO> getStudentByProgram(ProgramID programID){
        List<Student> students = studentRepository.findStudentByProgramId(programID);
        if(students.isEmpty()){
            return null;
        } else {
            return students.stream()
                    .map(this::convertStudentToDto)
                    .toList();
        }
    }

    @Override
    public ResponseEntity<NewAdminDTO> saveAdmin(@NotNull NewAdminDTO newAdminDTO) {
        Admin admin = modelMapper.map(newAdminDTO, Admin.class);
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setPasswordChanged(false);
        userRepository.save(admin);
        return ResponseEntity.ok().body(newAdminDTO);
    }

    @Override
    public ResponseEntity<NewStudentDTO> saveStudent(@NotNull NewStudentDTO studentDTO) {
        Student student = modelMapper.map(studentDTO, Student.class);
        student.setPassword(passwordEncoder.encode("123456"));
        student.setPasswordChanged(false);
        userRepository.save(student);
        return ResponseEntity.ok().body(studentDTO);
    }

    @Override
    public ResponseEntity<String> changePW(@NotNull ChangePWDTO pwDTO){
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userRepository.findByEmail(userEmail);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return processPasswordChange(user, pwDTO);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<String> resetPW(String targetUserEmail){
        Optional<User> optionalTargetUser = userRepository.findByEmail(targetUserEmail);
        String loggedInUserEmail = getCurrentUserEmail();
        if (optionalTargetUser.isPresent()) {
            User targetUser = optionalTargetUser.get();
            return processPasswordReset(targetUser, loggedInUserEmail);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public List<InfoStatusChangesDTO> getChanges(){
        List<PaymentStatusChange> changes = paymentStatusChangeRepository.findAll();
        return changes.stream().map(this::convertChanges).toList();
    }

    @Override
    public List<InfosAdminDTO> getAdmins(){
        List<Admin> admins = adminRepository.findAll();
        return admins.stream().map(this::convertAdminToDto).toList();
    }

    @Override
    public void emptyBlacklistTokens(){
        Instant day = Instant.now().minus(24, ChronoUnit.HOURS);
        blacklistedTokenRepository.deleteAllByBlacklistedAtLessThan(day);
    }

    //PRIVATE METHODS

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

    private InfosStudentDTO convertStudentToDto(Student student) {
        return modelMapper.map(student, InfosStudentDTO.class);
    }

    private InfosAdminDTO convertAdminToDto(Admin admin) {
        return modelMapper.map(admin, InfosAdminDTO.class);
    }

    private ResponseEntity<String> processPasswordChange(User user, ChangePWDTO pwDTO) {
        if (!passwordEncoder.matches(pwDTO.getOldPassword(), user.getPassword()) || pwDTO.getNewPassword().equals(pwDTO.getOldPassword())) {
            return ResponseEntity.badRequest().body("The new password must be different from the current one/Current password is not correct. ");
        }
        user.setPassword(passwordEncoder.encode(pwDTO.getNewPassword()));
        user.setPasswordChanged(true);
        userRepository.save(user);
        oldTokensProcessing(null);
        return ResponseEntity.ok("Password has been changed");
    }

    private void oldTokensProcessing(String userEmail){
        if(userEmail == null) {
            userEmail = getCurrentUserEmail();
        }
        Optional<List<UserTokens>> optionalUserTokens = userTokensRepository.findByEmail(userEmail);
        if (optionalUserTokens.isPresent()){
            List<UserTokens> userTokens = optionalUserTokens.get();
            userTokens.forEach(userToken -> {
                String token = userToken.getToken();
                securityService.logout(token);
            });
        }
    }

    private ResponseEntity<String> processPasswordReset(User targetUser, String loggedInUserEmail) {
        if (!targetUser.getEmail().equals(loggedInUserEmail)) {
            targetUser.setPassword(passwordEncoder.encode("123456"));
            targetUser.setPasswordChanged(false);
            userRepository.save(targetUser);
            oldTokensProcessing(targetUser.getEmail());
            return ResponseEntity.ok("Password has been reset");
        }
        return ResponseEntity.badRequest().body("You can't reset your own password! instead you can change it.");
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