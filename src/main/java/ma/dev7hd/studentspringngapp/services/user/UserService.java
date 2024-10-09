package ma.dev7hd.studentspringngapp.services.user;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.PictureDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.entities.registrations.BanedRegistration;
import ma.dev7hd.studentspringngapp.entities.tokens.UserTokens;
import ma.dev7hd.studentspringngapp.entities.users.Admin;
import ma.dev7hd.studentspringngapp.entities.registrations.PendingStudent;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.entities.users.User;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.registrations.BanedRegistrationRepository;
import ma.dev7hd.studentspringngapp.repositories.registrations.PendingStudentRepository;
import ma.dev7hd.studentspringngapp.repositories.tokens.UserTokensRepository;
import ma.dev7hd.studentspringngapp.repositories.users.AdminRepository;
import ma.dev7hd.studentspringngapp.repositories.users.StudentRepository;
import ma.dev7hd.studentspringngapp.repositories.users.UserRepository;
import ma.dev7hd.studentspringngapp.services.dataFromFile.image.IImageService;
import ma.dev7hd.studentspringngapp.services.global.IUserDataProvider;
import ma.dev7hd.studentspringngapp.services.notification.INotificationService;
import ma.dev7hd.studentspringngapp.security.services.ISecurityService;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ISecurityService securityService;
    private final UserTokensRepository userTokensRepository;
    private final AdminRepository adminRepository;
    private final ModelMapper modelMapper;
    private final PendingStudentRepository pendingStudentRepository;
    private final BanedRegistrationRepository banedRegistrationRepository;
    private final INotificationService notificationService;
    private final IUserDataProvider iUserDataProvider;

    private final String DEFAULT_PASSWORD = "123456";
    private final Path PATH_TO_PHOTOS = Paths.get(System.getProperty("user.home"), "data", "photos");
    private final Path PATH_TO_REGISTRATION_PHOTOS = Paths.get(System.getProperty("user.home"), "data", "registrations_photos");
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final IImageService imageService;


    @Override
    @Transactional
    public ResponseEntity<String> deleteUserByEmail(String email) {
        Optional<User> optionalUser = userRepository.findById(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            userRepository.delete(user);
            return ResponseEntity.ok().body("User deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @Transactional
    @Override
    public InfosAdminDTO updateAdmin(InfosAdminDTO adminDTO){
        return adminRepository.findById(adminDTO.getEmail())
                .map(admin -> {
                    admin.setFirstName(adminDTO.getFirstName());
                    admin.setLastName(adminDTO.getLastName());
                    admin.setDepartmentName(adminDTO.getDepartmentName());
                    Admin saved = adminRepository.save(admin);
                    return convertAdminToDto(saved);
                })
                .orElse(null);
    }

    @Transactional
    @Override
    public Boolean toggleAdminAccount(String email) throws ChangeSetPersister.NotFoundException {
        return adminRepository.findById(email)
                .map(admin -> {
                    admin.setEnabled(!admin.isEnabled());
                    adminRepository.save(admin);
                    return admin.isEnabled();
                })
                .orElseThrow(ChangeSetPersister.NotFoundException::new);
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
    @Transactional
    public ResponseEntity<NewAdminDTO> saveAdmin(@NotNull NewAdminDTO newAdminDTO) {
        Admin admin = newAdminProcessing(modelMapper.map(newAdminDTO, Admin.class));
        userRepository.save(admin);
        return ResponseEntity.ok().body(newAdminDTO);
    }

    @Override
    @Transactional
    public ResponseEntity<InfosStudentDTO> saveStudent(@NotNull NewStudentDTO studentDTO, MultipartFile photo) throws IOException {
        Student student = newStudentProcessing(modelMapper.map(studentDTO, Student.class), photo.getBytes());
        Student saved = userRepository.save(student);
        return ResponseEntity.ok().body(convertStudentToDto(saved));
    }

    @Override
    @Transactional
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
    @Transactional
    public ResponseEntity<String> resetPW(String targetUserEmail){
        Optional<User> optionalTargetUser = userRepository.findByEmail(targetUserEmail);
        String loggedInUserEmail = iUserDataProvider.getCurrentUserEmail();
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
    @Transactional
    public ResponseEntity<String> registerStudent(@NotNull NewPendingStudentDTO pendingStudentDTO, MultipartFile photo) throws IOException {
        boolean userIsExistByEmail = userRepository.existsById(pendingStudentDTO.getEmail());
        boolean pendingStudentExistByEmailOrCode = pendingStudentRepository.existsById(pendingStudentDTO.getEmail());
        boolean bannedExistById = banedRegistrationRepository.existsById(pendingStudentDTO.getEmail());

        if (userIsExistByEmail || pendingStudentExistByEmailOrCode || bannedExistById) {
            return ResponseEntity.badRequest().body("Email or Code already in use or banned");
        }
        String fileName = UUID.randomUUID().toString();
        String extension = FilenameUtils.getExtension(photo.getOriginalFilename());
        Path filePath = PATH_TO_REGISTRATION_PHOTOS.resolve(fileName + "." + extension);
        if (!Files.exists(PATH_TO_REGISTRATION_PHOTOS)) {
            Files.createDirectories(PATH_TO_REGISTRATION_PHOTOS);
        }
        try {
            Files.copy(photo.getInputStream(), filePath);
        } catch (IOException e) {
            return null;
        }
        File file = new File(filePath.toString());
        String photoUri = file.toURI().toString();

        PendingStudent pendingStudent = convertPendingStudentToDto(pendingStudentDTO);

        pendingStudent.setPhoto(photoUri);
        pendingStudent.setRegisterDate(new Date());

        PendingStudent savedPendingStudent = pendingStudentRepository.save(pendingStudent);
        notificationService.sendPendingStudentNotifications(savedPendingStudent);

        return ResponseEntity.ok().body("The registration was successful.");
    }

    @Override
    public Page<PendingStudent> getPendingStudent(String email, int page, int size){
        Page<PendingStudent> pendingStudents = pendingStudentRepository.findByPendingStudentsByFilter(email, PageRequest.of(page, size));
        notificationService.seenNewRegistration(pendingStudents.getContent());
        return pendingStudents;
    }

    @Override
    public ResponseEntity<PendingStudent> getPendingStudentByEmail(String email){
        Optional<PendingStudent> optionalPendingStudent = pendingStudentRepository.findById(email);
        if (optionalPendingStudent.isPresent()) {
            PendingStudent pendingStudent = optionalPendingStudent.get();
            notificationService.adminNotificationSeen(null,pendingStudent.getEmail());
            return ResponseEntity.ok(pendingStudent);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    @Transactional
    public ResponseEntity<?> approvingStudentRegistration(@NotNull String email) throws IOException {
        Optional<PendingStudent> optionalPendingStudent = pendingStudentRepository.findById(email);
        if (optionalPendingStudent.isPresent()) {
            if (!studentRepository.existsByEmail(optionalPendingStudent.get().getEmail())){
                PendingStudent pendingStudent = optionalPendingStudent.get();
                byte[] photo = Files.readAllBytes(Path.of(URI.create(pendingStudent.getPhoto())));
                Student approvedStudent = newStudentProcessing(convertPendingStudentToStudent(pendingStudent), photo);
                String savePhoto = savePhoto(photo, approvedStudent.getCode());
                approvedStudent.setPhoto(savePhoto);
                Files.deleteIfExists(Path.of(URI.create(pendingStudent.getPhoto())));
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
    @Transactional
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
    @Transactional
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

    @Transactional
    @Override
    public ResponseEntity<String> banStudentRegistration(@NotNull String email){
        Optional<PendingStudent> optionalPendingStudent = pendingStudentRepository.findById(email);
        Optional<User> optionalUser = userRepository.findById(iUserDataProvider.getCurrentUserEmail());
        if (optionalPendingStudent.isPresent() && optionalUser.isPresent() && optionalUser.get() instanceof Admin admin) {
            PendingStudent pendingStudent = optionalPendingStudent.get();

            BanedRegistration banedRegistration = convertPendingStudentToBanedRegistration(pendingStudent);
            banedRegistration.setBanDate(new Date());
            banedRegistration.setAdminBanner(admin);

            banedRegistrationRepository.save(banedRegistration);
            pendingStudentRepository.delete(pendingStudent);
            return ResponseEntity.ok().body("The registration was banned successfully.");
        }
        return ResponseEntity.badRequest().build();
    }

    @Override
    @Transactional
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
    @Transactional
    public ResponseEntity<String> approveMultipleRegistrations(List<String> emails) {
        try {
            List<PendingStudent> allPendingStudents = pendingStudentRepository.findAllById(emails);
            StringBuilder message = new StringBuilder();
            if (allPendingStudents.isEmpty()) {
                message.append("No registration was approved. Registrations provided doesn't exist!");
                return ResponseEntity.ok(message.toString());
            }

            Set<String> allEmails = studentRepository.findAllEmails();

            // Parallel processing for approving registrations
            CompletableFuture<List<Student>> approveFuture = CompletableFuture.supplyAsync(() -> allPendingStudents.parallelStream()
                    .filter(registration ->
                            !allEmails.contains(registration.getEmail())
                    ).map(registration -> {
                        Student student = new Student();
                        student.setEmail(registration.getEmail());
                        student.setFirstName(registration.getFirstName());
                        student.setLastName(registration.getLastName());
                        student.setEnabled(true);
                        student.setCode(iUserDataProvider.generateStudentCode(registration.getProgramID(), null));
                        student.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                        student.setPasswordChanged(false);
                        student.setProgramId(registration.getProgramID());
                        return student;
                    }).collect(Collectors.toList()), executorService);

            List<Student> studentsToApprove = approveFuture.join(); // Wait for completion

            studentRepository.saveAll(studentsToApprove);

            List<String> registrationsEmails = studentsToApprove.stream()
                    .map(Student::getEmail)
                    .collect(Collectors.toList());

            pendingStudentRepository.deleteAllById(registrationsEmails);

            int approvedCount = studentsToApprove.size();
            int foundCount = allPendingStudents.size();
            int totalProvided = emails.size();

            if (approvedCount == 0) {
                message.append("No registration was approved!");
            } else if (approvedCount < foundCount && foundCount < totalProvided) {
                message.append(approvedCount).append(" registration(s) approved, ")
                        .append(foundCount - approvedCount).append(" cannot be approved, ")
                        .append(totalProvided - foundCount).append(" not found.");
            } else if (approvedCount < foundCount && foundCount == totalProvided) {
                message.append(approvedCount).append(" registration(s) approved, ")
                        .append(foundCount - approvedCount).append(" cannot be approved.");
            } else if (approvedCount == foundCount && approvedCount < totalProvided) {
                message.append(approvedCount).append(" registration(s) approved, ")
                        .append(totalProvided - approvedCount).append(" not found.");
            } else {
                message.append("All selected registrations were approved.");
            }

            return ResponseEntity.ok(message.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing registrations: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> banMultipleRegistrations(List<String> emails) {
        try {
            List<PendingStudent> pendingStudents = pendingStudentRepository.findAllById(emails);
            if (pendingStudents.isEmpty()) return ResponseEntity.badRequest().body("No registration was found!");

            Optional<Admin> currentAdmin = iUserDataProvider.getCurrentAdmin();
            if (currentAdmin.isPresent()) {
                Admin admin = currentAdmin.get();

                // Parallel processing for banning registrations
                CompletableFuture<Void> banFuture = CompletableFuture.runAsync(() -> {
                    List<BanedRegistration> registrationsToBan = pendingStudents.parallelStream()
                            .map(pendingStudent -> BanedRegistration.builder()
                                    .banDate(new Date())
                                    .registerDate(pendingStudent.getRegisterDate())
                                    .email(pendingStudent.getEmail())
                                    .firstName(pendingStudent.getFirstName())
                                    .lastName(pendingStudent.getLastName())
                                    .programID(pendingStudent.getProgramID())
                                    .adminBanner(admin)
                                    .build())
                            .collect(Collectors.toList());

                    banedRegistrationRepository.saveAll(registrationsToBan);
                    pendingStudentRepository.deleteAll(pendingStudents);
                }, executorService);

                banFuture.join(); // Wait for completion

                return ResponseEntity.ok(pendingStudents.size() + " registration(s) was banned.");
            }
            return ResponseEntity.badRequest().body("The request must be done by a valid admin.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ban registrations error: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void declineMultipleRegistrations(List<String> emails){
        try {
            pendingStudentRepository.deleteAllById(emails);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> deleteMultipleUsers(List<String> emails){
        List<User> users = userRepository.findAllById(emails);
        if (users.isEmpty()) return ResponseEntity.badRequest().body("Emails provided not valid.");
        userRepository.deleteAll(users);
        StringBuilder message = new StringBuilder();
        int providedCount = emails.size();
        int usersFoundCount = users.size();
        if (providedCount == usersFoundCount) message.append("All provided users was successfully deleted.");
        else message.append(usersFoundCount).append(" was deleted and ").append(providedCount - usersFoundCount).append(" not didn't.");
        return ResponseEntity.ok(message.toString());
    }

    @Override
    public ResponseEntity<String> resetPasswordToMultipleUsers(List<String> emails) {
        List<User> users = userRepository.findAllById(emails);
        users.forEach(user -> {
            user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
            user.setPasswordChanged(false);
        });
        userRepository.saveAll(users);
        return ResponseEntity.ok(users.size() + " password(s) has been reset successfully.");
    }

    @Override
    public void toggleUserAccount(List<String> emails){
        List<User> users = userRepository.findAllById(emails);
        if (!users.isEmpty()) {
            List<User> toggledUsers = users.stream()
                    .peek(user -> user.setEnabled(!user.isEnabled())).toList();
            userRepository.saveAll(toggledUsers);
        }
    }

    @Override
    public PictureDTO getProfilePicture(String email) throws IOException {
        Optional<Student> optionalStudent = studentRepository.findById(email);
        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            PictureDTO pictureDTO = new PictureDTO();
            pictureDTO.setPicture(Files.readAllBytes(Path.of(URI.create(student.getPhoto()))));
            pictureDTO.setPictureName(student.getCode());
            return pictureDTO;
        }
        return null;
    }

    //PRIVATE METHODS

    private Student newStudentProcessing(Student student, byte[] photo) throws IOException {
        String studentCode = iUserDataProvider.generateStudentCode(student.getProgramId(), null);

        if (photo != null){
            byte[] newPhoto = imageService.resizeImageWithAspectRatio(photo);
            String photoUri = savePhoto(newPhoto, studentCode);
            student.setPhoto(photoUri);
        }

        student.setPasswordChanged(false);
        student.setEnabled(true);
        student.setCode(studentCode);
        student.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        Student.updateProgramCountsFromDB(student.getProgramId(),1.0);
        return student;
    }

    private String savePhoto(byte[] photo, String studentCode){
        String fileName = studentCode + ".jpg";
        Path filePath = PATH_TO_PHOTOS.resolve(fileName);
        try {
            if (!Files.exists(PATH_TO_PHOTOS)) {
                Files.createDirectories(PATH_TO_PHOTOS);
            }
            Files.write(filePath, photo);
        } catch (IOException e) {
            return null;
        }
        File file = new File(filePath.toString());
        return file.toURI().toString();
    }

    private Admin newAdminProcessing(Admin admin){
        admin.setPasswordChanged(false);
        admin.setEnabled(true);
        admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
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
            userEmail = iUserDataProvider.getCurrentUserEmail();
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
            targetUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
            targetUser.setPasswordChanged(false);
            userRepository.save(targetUser);
            oldTokensProcessing(targetUser.getEmail());
            return ResponseEntity.ok("Password has been reset");
        }
        return ResponseEntity.badRequest().body("You can't reset your own password! instead you can change it.");
    }

}
