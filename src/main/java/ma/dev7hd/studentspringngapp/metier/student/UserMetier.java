package ma.dev7hd.studentspringngapp.metier.student;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.entities.*;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.*;
import ma.dev7hd.studentspringngapp.security.services.ISecurityService;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class UserMetier implements IUserMetier {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ISecurityService securityService;
    private final UserTokensRepository userTokensRepository;
    private final AdminRepository adminRepository;
    private final ModelMapper modelMapper;
    private final PendingStudentRepository pendingStudentRepository;

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
    public List<InfosAdminDTO> getAdmins(){
        List<Admin> admins = adminRepository.findAll();
        return admins.stream().map(this::convertAdminToDto).toList();
    }

    @Override
    public Page<InfosAdminDTO> getAdminsByCriteria(String email, String firstName, String lastName, DepartmentName departmentName, int page, int size){
        Page<Admin> admins = adminRepository.findByFilter(email, firstName, lastName, departmentName, PageRequest.of(page, size));
        return convertPageableAdminToDTO(admins);
    }

    @Override
    public Page<InfosStudentDTO> getStudentsByCriteria(String email, String firstName, String lastName, ProgramID programID, String code, int page, int size){
        Page<Student> students = studentRepository.findByFilter(email, firstName, lastName, programID, code, PageRequest.of(page, size));
        return convertPageableStudentToDTO(students);
    }

    @Override
    public ResponseEntity<String> registerStudent(@NotNull NewPendingStudentDTO pendingStudentDTO){
        Optional<User> optionalUser = userRepository.findById(pendingStudentDTO.getEmail());
        Optional<Student> optionalStudent = studentRepository.findStudentByCode(pendingStudentDTO.getCode());
        if (optionalUser.isPresent() || optionalStudent.isPresent()) {
            return ResponseEntity.badRequest().body("Email or Code already in use");
        }
        PendingStudent pendingStudent = convertPendingStudentToDto(pendingStudentDTO);
        pendingStudent.setRegisterDate(Instant.now());
        pendingStudentRepository.save(pendingStudent);
        return ResponseEntity.ok().body("The registration was successful.");
    }

    @Override
    public ResponseEntity<?> approvingStudentRegistration(@NotNull String email){
        Optional<PendingStudent> optionalPendingStudent = pendingStudentRepository.findById(email);
        if (optionalPendingStudent.isPresent()) {
            PendingStudent pendingStudent = optionalPendingStudent.get();
            Student student = convertPendingStudentToStudent(pendingStudent);
            student.setPassword(passwordEncoder.encode("123456"));
            student.setPasswordChanged(false);
            Student savedStudent = studentRepository.save(student);
            pendingStudentRepository.delete(pendingStudent);
            return ResponseEntity.ok().body(convertStudentToDto(savedStudent));
        } else {
            return ResponseEntity.badRequest().body("Email is not correct.");
        }
    }

    //PRIVATE METHODS

    private Page<InfosAdminDTO> convertPageableAdminToDTO(Page<Admin> admins){
        return admins.map(admin -> modelMapper.map(admin, InfosAdminDTO.class));
    }

    private PendingStudent convertPendingStudentToDto(NewPendingStudentDTO pendingStudentDTO) {
        return modelMapper.map(pendingStudentDTO, PendingStudent.class);
    }

    private Student convertPendingStudentToStudent(PendingStudent pendingStudent) {
        return modelMapper.map(pendingStudent, Student.class);
    }

    private Page<InfosStudentDTO> convertPageableStudentToDTO(Page<Student> students){
        return students.map(student -> modelMapper.map(student, InfosStudentDTO.class));
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
                securityService.logoutByToken(token);
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

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
