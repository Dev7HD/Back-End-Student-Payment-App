package ma.dev7hd.studentspringngapp.metier.user;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.PendingUserDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.entities.*;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.*;
import ma.dev7hd.studentspringngapp.security.services.ISecurityService;
import ma.dev7hd.studentspringngapp.websoket.config.WebSocketService;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private final BanedRegistrationRepository banedRegistrationRepository;
    private final WebSocketService webSocketService;

    private final String defaultPassword = "123456";

    @Override
    public ResponseEntity<String> deleteUser(String email) {
        Optional<User> optionalUser = userRepository.findById(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(user instanceof Student) {
                Student.updateProgramCountsFromDB(((Student) user).getProgramId(), -1.0);
                paymentRepository.deleteByStudentCode(((Student) user).getCode());
            }
            userRepository.delete(user);
            return ResponseEntity.ok().body("User deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found");
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
            return List.of();
        } else {
            return students.stream()
                    .map(this::convertStudentToDto)
                    .toList();
        }
    }

    @Override
    public ResponseEntity<NewAdminDTO> saveAdmin(@NotNull NewAdminDTO newAdminDTO) {
        Admin admin = newAdminProcessing(modelMapper.map(newAdminDTO, Admin.class));
        userRepository.save(admin);
        return ResponseEntity.ok().body(newAdminDTO);
    }

    @Override
    public ResponseEntity<NewStudentDTO> saveStudent(@NotNull NewStudentDTO studentDTO) {
        Student student = newStudentProcessing(modelMapper.map(studentDTO, Student.class));
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
    public Page<InfosStudentDTO> getStudentsByCriteriaAsAdmin(String email, String firstName, String lastName, ProgramID programID, String code, int page, int size){
        Page<Student> students = studentRepository.findByFilter(email, firstName, lastName, programID, code, PageRequest.of(page, size));
        return convertPageableStudentToDTO(students);
    }

    @Override
    public ResponseEntity<String> registerStudent(@NotNull NewPendingStudentDTO pendingStudentDTO){
        Optional<User> optionalUser = userRepository.findById(pendingStudentDTO.getEmail());
        Optional<Student> optionalStudent = studentRepository.findStudentByCode(pendingStudentDTO.getCode());
        Optional<BanedRegistration> optionalBanedRegistration = banedRegistrationRepository.findById(pendingStudentDTO.getEmail());
        if (optionalUser.isPresent() || optionalStudent.isPresent() || optionalBanedRegistration.isPresent()) {
            return ResponseEntity.badRequest().body("Email or Code already in use or banned");
        }
        PendingStudent pendingStudent = convertPendingStudentToDto(pendingStudentDTO);
        pendingStudent.setRegisterDate(new Date());
        pendingStudent.setSeen(false);
        PendingStudent savedPendingStudent = pendingStudentRepository.save(pendingStudent);
        PendingUserDTO pendingUserDTO = modelMapper.map(savedPendingStudent, PendingUserDTO.class);
        String message = savedPendingStudent.getFirstName() + " " + savedPendingStudent.getLastName() + " made a new registration need to be processed.";
        pendingUserDTO.setMessage(message);
        webSocketService.sendToSpecificUser("/notifications/pending-registration", pendingUserDTO);
        return ResponseEntity.ok().body("The registration was successful.");
    }

    @Override
    public Page<PendingStudent> getPendingStudent(String email, boolean isSeen, int page, int size){
        Page<PendingStudent> pendingStudents = pendingStudentRepository.findByPendingStudentsByFilter(email, isSeen, PageRequest.of(page, size));
        pendingStudents.forEach(pendingStudent -> {
            pendingStudent.setSeen(true);
            pendingStudentRepository.save(pendingStudent);
        });
        return pendingStudents;
    }

    @Override
    public void onLoginNotifications(){
        List<PendingStudent> pendingStudents = pendingStudentRepository.findAllBySeen(false);

        pendingStudents.forEach(pendingStudent -> {
            PendingUserDTO pendingUserDTO = modelMapper.map(pendingStudent, PendingUserDTO.class);
            String message = pendingStudent.getFirstName() + " " + pendingStudent.getLastName() + " made a new registration need to be processed.";
            pendingUserDTO.setMessage(message);
            webSocketService.sendToSpecificUser("/notifications/pending-registration", pendingUserDTO);
        });


    }

    @Override
    public ResponseEntity<?> approvingStudentRegistration(@NotNull String email){
        Optional<PendingStudent> optionalPendingStudent = pendingStudentRepository.findById(email);
        if (optionalPendingStudent.isPresent()) {
            if (!studentRepository.existsByEmailOrCode(optionalPendingStudent.get().getEmail(), optionalPendingStudent.get().getCode())){
                PendingStudent pendingStudent = optionalPendingStudent.get();
                Student approvedStudent = newStudentProcessing(convertPendingStudentToStudent(pendingStudent));
                Student savedStudent = studentRepository.save(approvedStudent);
                pendingStudentRepository.delete(pendingStudent);
                return ResponseEntity.ok().body(convertStudentToDto(savedStudent));
            }
            return ResponseEntity.badRequest().body("Student already registered.");
        } else {
            return ResponseEntity.badRequest().body("Email is not correct.");
        }
    }

    @Override
    public ResponseEntity<String> declineStudentRegistration(@NotNull String email){
        Optional<PendingStudent> optionalPendingStudent = pendingStudentRepository.findById(email);
        if (optionalPendingStudent.isPresent()) {
            PendingStudent pendingStudent = optionalPendingStudent.get();
            pendingStudentRepository.delete(pendingStudent);
            return ResponseEntity.ok().body("The registration was declined successfully.");
        }
        return ResponseEntity.badRequest().body("Email is not correct.");
    }

    @Override
    public ResponseEntity<String> toggleEnableUserAccount(String email){
        Optional<User> optionalUser = userRepository.findById(email);
        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
            String accountStatus;
            if(user.isEnabled()){
                accountStatus = "enabled";
            } else {
                accountStatus = "disabled";
            }
            return ResponseEntity.ok().body("User account has been " + accountStatus + ".");
        }
        return ResponseEntity.badRequest().body("Email is not correct.");
    }

    @Override
    public ResponseEntity<String> banStudentRegistration(@NotNull String email){
        Optional<PendingStudent> optionalPendingStudent = pendingStudentRepository.findById(email);
        if (optionalPendingStudent.isPresent()) {
            PendingStudent pendingStudent = optionalPendingStudent.get();
            BanedRegistration banedRegistration = convertPendingStudentToBanedRegistration(pendingStudent);
            banedRegistration.setBanDate(Instant.now());
            Optional<Admin> admin = adminRepository.findById(getCurrentUserEmail());
            admin.ifPresent(banedRegistration::setAdminBanner);
            banedRegistrationRepository.save(banedRegistration);
            pendingStudentRepository.delete(pendingStudent);
            return ResponseEntity.ok().body("The registration was banned successfully.");
        }
        return ResponseEntity.badRequest().body("Email is not correct.");
    }

    @Override
    public List<PendingStudent> getPendingStudents(){
        return pendingStudentRepository.findAll();
    }

    @Override
    public ResponseEntity<InfosStudentDTO> updateStudentInfo(@NotNull InfosStudentDTO studentDTO){
        Optional<Student> optionalStudent = studentRepository.findById(studentDTO.getEmail());
        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            student.setFirstName(studentDTO.getFirstName());
            student.setLastName(studentDTO.getLastName());
            student.setProgramId(studentDTO.getProgramId());
            student.setCode(studentDTO.getCode());
            Student savedStudentInfo = studentRepository.save(student);
            Student.updateProgramCountsFromDB(student.getProgramId(), -1.0);
            Student.updateProgramCountsFromDB(studentDTO.getProgramId(), 1.0);
            return ResponseEntity.ok(convertStudentToDto(savedStudentInfo));
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    public Map<ProgramID, List<Double>> getProgramIdCounts(){
        return Student.programIDCounter;
    }

    //PRIVATE METHODS

    private Student newStudentProcessing(Student student){
        student.setPasswordChanged(false);
        student.setEnabled(true);
        student.setPassword(passwordEncoder.encode(defaultPassword));
        Student.updateProgramCountsFromDB(student.getProgramId(),1.0);
        return student;
    }

    private Admin newAdminProcessing(Admin admin){
        admin.setPasswordChanged(false);
        admin.setEnabled(true);
        admin.setPassword(passwordEncoder.encode(defaultPassword));
        return admin;
    }

    private Page<InfosAdminDTO> convertPageableAdminToDTO(Page<Admin> admins){
        return admins.map(admin -> modelMapper.map(admin, InfosAdminDTO.class));
    }

    private BanedRegistration convertPendingStudentToBanedRegistration(PendingStudent pendingStudent){
        return modelMapper.map(pendingStudent, BanedRegistration.class);
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
            targetUser.setPassword(passwordEncoder.encode(defaultPassword));
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
