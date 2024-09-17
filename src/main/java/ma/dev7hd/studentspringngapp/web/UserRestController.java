package ma.dev7hd.studentspringngapp.web;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.entities.PendingStudent;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.services.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/user")
public class UserRestController {
    private final IUserService iUserService;

    /**
     * Get all students
     *
     * @return List<Student>
     */
    @GetMapping(path = "/student/all")
    //@PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN')")
    public List<InfosStudentDTO> getAllStudents() {
        return iUserService.getAllStudents();
    }

    /**
     * Get All admins
     *
     * @return List<InfosAdminDTO>
     */
    @GetMapping(path = "/admin/all")
    //@PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN')")
    public List<InfosAdminDTO> getAllAdmin() {
        return iUserService.getAllAdmins();
    }

    /**
     * Find all students by program
     *
     * @param programId is student program id
     * @return List<Student>
     */
    @GetMapping(path = "/student/program/{programId}")
    //@PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN')")
    public List<InfosStudentDTO> getStudentsByProgramId(@PathVariable ProgramID programId) {
        return iUserService.getStudentByProgram(programId);
    }

    /**
     * Find student by his code
     *
     * @param code is student code
     * @return Optional<Student>
     */
    @GetMapping(path = "/student/code/{code}")
    public ResponseEntity<InfosStudentDTO> getStudentByCode(@PathVariable String code) {
        return iUserService.getStudentByCode(code);
    }

    /**
     * Get student by his email
     *
     * @param email is user email
     * @return ResponseEntity<InfosStudentDTO>
     */
    @GetMapping(path = "/student/email/{email}")
    public ResponseEntity<InfosStudentDTO> getStudentByEmail(@PathVariable String email) {
        return iUserService.getStudentById(email);
    }

    /**
     * Delete user by providing his email
     *
     * @param email is user email
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUserByEmail(String email) {
        return iUserService.deleteUserByEmail(email);
    }

    @PutMapping("/student/update")
    public ResponseEntity<InfosStudentDTO> updateStudentInfo(@RequestBody InfosStudentDTO infosStudentDTO) {
        return iUserService.updateStudent(infosStudentDTO);
    }

    /**
     * Saving a new student
     *
     * @param studentDTO student information (email, firstName, lastName, code and programId)
     * @return ResponseEntity<NewStudentDTO>
     */
    @PostMapping("/student/new")
    public ResponseEntity<NewStudentDTO> saveStudent(@RequestBody NewStudentDTO studentDTO) {
        return iUserService.addStudent(studentDTO);
    }

    /**
     * Saving a new student
     *
     * @param adminDTO admin information (email, firstName, lastName and departmentName)
     * @return ResponseEntity<NewAdminDTO>
     */
    @PostMapping("/admin/new")
    public ResponseEntity<NewAdminDTO> saveAdmin(@RequestBody NewAdminDTO adminDTO) {
        return iUserService.addAdmin(adminDTO);
    }

    /**
     * Change user password
     *
     * @param changePWDTO (oldPassword and newPassword)
     * @return ResponseEntity<String>
     */
    @PutMapping("/change-pw")
    public ResponseEntity<String> changePassword(@RequestBody ChangePWDTO changePWDTO) {
        return iUserService.changePassword(changePWDTO);
    }

    /**
     * Reset the password to the default value (123456)
     *
     * @param email the user email that we want to reset his password
     * @return ResponseEntity<String>
     */
    @PutMapping("/{email}/reset-pw")
    public ResponseEntity<String> resetPassword(@PathVariable String email) {
        return iUserService.resetPW(email);
    }

    /**
     * Get all admins with backend pagination and multi-criteria filtering
     *
     * @param email          admin email
     * @param firstName      admin first name
     * @param lastName       admin last name
     * @param departmentName admin department name
     * @param page           page number
     * @param size           items per page
     * @return Page<InfosAdminDTO>
     */
    @GetMapping("/admin")
    public Page<InfosAdminDTO> getAdminsByCriteria(@RequestParam(defaultValue = "") String email,
                                                   @RequestParam(defaultValue = "") String firstName,
                                                   @RequestParam(defaultValue = "") String lastName,
                                                   @RequestParam(defaultValue = "") DepartmentName departmentName,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return iUserService.getAdminsByCriteria(email, firstName, lastName, departmentName, page, size);
    }

    /**
     * Get all students with backend pagination and multi-criteria filtering
     *
     * @param email     student email
     * @param firstName student first name
     * @param lastName  student last name
     * @param programID student study program
     * @param code      student code
     * @param page      page number
     * @param size      items per page
     * @return Page<InfosStudentDTO>
     */
    @GetMapping("/student")
    public Page<InfosStudentDTO> getStudentsByCriteria(@RequestParam(defaultValue = "") String email,
                                                       @RequestParam(defaultValue = "") String firstName,
                                                       @RequestParam(defaultValue = "") String lastName,
                                                       @RequestParam(defaultValue = "") ProgramID programID,
                                                       @RequestParam(defaultValue = "") String code,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return iUserService.getStudentsByCriteria(email, firstName, lastName, programID, code, page, size);
    }

    /**
     * Register new student that stays in pending list until be approved or declined
     *
     * @param pendingStudentDTO the new student information
     * @return ResponseEntity<String>
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(@RequestBody NewPendingStudentDTO pendingStudentDTO) {
        return iUserService.registerStudent(pendingStudentDTO);
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approvingStudentRegistration(String email) {
        return iUserService.approvingStudentRegistration(email);
    }

    @PostMapping("/decline")
    public ResponseEntity<String> declineStudentRegistration(String email) {
        return iUserService.declineStudentRegistration(email);
    }

    @PostMapping("/ban")
    public ResponseEntity<String> banStudentRegistration(String email) {
        return iUserService.banStudentRegistration(email);
    }

    @PatchMapping("/toggle-account-status")
    ResponseEntity<String> toggleEnableUserAccount(String email) {
        return iUserService.toggleEnableUserAccount(email);
    }

    @GetMapping("/pending-students")
    public Page<PendingStudent> getPendingStudent(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean seen,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return iUserService.getPendingStudent(email, seen, page, size);
    }

    @GetMapping("/pending-student/{email}")
    public ResponseEntity<PendingStudent> getPendingStudentByEmail(@PathVariable String email) {
        return iUserService.getPendingStudentByEmail(email);
    }

    @PostMapping(value = "/students/load-from-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadStudentFile(@Parameter(description = "XLS or XLSX to upload") @RequestPart(value = "file")MultipartFile file) throws Exception {
        return iUserService.uploadStudentFile(file);
    }

    @PostMapping("/delete-notification")
    public void deleteStudentRegistrationNotification(String email){
        iUserService.deleteStudentRegistrationNotification(email);
    }
}
