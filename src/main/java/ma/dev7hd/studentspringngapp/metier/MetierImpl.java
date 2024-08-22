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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
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
public class MetierImpl implements IMetier {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusChangeRepository paymentStatusChangeRepository;
    private ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final ISecurityService securityService;
    private final UserTokensRepository userTokensRepository;

    @Override
    public ResponseEntity<Payment> saveNewPayment(NewPaymentDTO newPaymentDTO,
                                                  @org.jetbrains.annotations.NotNull MultipartFile file) throws IOException {
        if (!Objects.equals(file.getContentType(), MediaType.APPLICATION_PDF_VALUE)) {
            return ResponseEntity.badRequest().build();
        }

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userRepository.findByEmail(userEmail);

        Optional<Student> optionalStudent = studentRepository.findStudentByCode(newPaymentDTO.getStudentCode());

        if (optionalStudent.isPresent() && optionalUser.isPresent()) {
            Student student = optionalStudent.get();
            User user = optionalUser.get();

            Path folderPath = Paths.get(System.getProperty("user.home"), "data", "payments");
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            String fileName = UUID.randomUUID().toString();
            Path filePath = Paths.get(folderPath.toString(), fileName + ".pdf");

            try {
                Files.copy(file.getInputStream(), filePath);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            Payment payment = Payment.builder()
                    .amount(newPaymentDTO.getAmount())
                    .student(student)
                    .type(newPaymentDTO.getPaymentType())
                    .date(newPaymentDTO.getDate())
                    .status(PaymentStatus.CREATED)
                    .receipt(filePath.toUri().toString())
                    .addedBy(user)
                    .build();
            paymentRepository.save(payment);
            return ResponseEntity.ok(payment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<InfoPaymentDTO> updatePaymentStatus(UUID paymentId, PaymentStatus newStatus) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalAdmin = userRepository.findByEmail(userEmail);

        if (optionalPayment.isPresent() && optionalAdmin.isPresent()) {
            Payment payment = optionalPayment.get();
            User user = optionalAdmin.get();

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
                payment = paymentRepository.save(payment);

                InfoPaymentDTO infoPaymentDTO = modelMapper.map(payment, InfoPaymentDTO.class);
                return ResponseEntity.ok(infoPaymentDTO);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
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
        return ResponseEntity.accepted().body(newAdminDTO);
    }

    @Override
    public ResponseEntity<NewStudentDTO> saveStudent(@NotNull NewStudentDTO studentDTO) {
        Student student = modelMapper.map(studentDTO, Student.class);
        student.setPassword(passwordEncoder.encode("123456"));
        student.setPasswordChanged(false);
        userRepository.save(student);
        return ResponseEntity.accepted().body(studentDTO);
    }

    @Override
    public ResponseEntity<String> changePW(@NotNull ChangePWDTO changePWDTO){
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userRepository.findByEmail(userEmail);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(changePWDTO.getOldPassword(), user.getPassword())){
                if(!Objects.equals(changePWDTO.getOldPassword(), changePWDTO.getNewPassword())){
                    user.setPassword(passwordEncoder.encode(changePWDTO.getNewPassword()));
                    user.setPasswordChanged(true);
                    userRepository.save(user);
                    String currentUserToken = getCurrentUserToken();
                    securityService.logout(currentUserToken);
                    System.out.println("token blacklisted after change password");
                    return ResponseEntity.accepted().body("Password has been changed");
                } else {
                    return ResponseEntity.badRequest().body("The new password must be different from the old one.");
                }

            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<String> resetPW(String userEmail){
        Optional<User> optionalUser = userRepository.findByEmail(userEmail);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode("123456"));
            user.setPasswordChanged(false);
            userRepository.save(user);
            Optional<UserTokens> userToken = userTokensRepository.findById(userEmail);
            if (userToken.isPresent()){
                String token = userToken.get().getToken();
                securityService.logout(token);
                System.out.println("token blacklisted after reset password");
            }
            return ResponseEntity.accepted().body("Password has been reset");
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

    private String getCurrentUserToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            return jwt.getTokenValue();
        }
        return null;
    }
}
