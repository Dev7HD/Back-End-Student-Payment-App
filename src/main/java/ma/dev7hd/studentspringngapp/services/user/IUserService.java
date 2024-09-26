package ma.dev7hd.studentspringngapp.services.user;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.entities.registrations.PendingStudent;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IUserService {

    @Transactional
    ResponseEntity<String> deleteUserByEmail(String email);

    List<InfosStudentDTO> getAllStudents();

    ResponseEntity<InfosStudentDTO> getStudentByCode(String code);

    ResponseEntity<InfosStudentDTO> getStudentById(String email);

    List<InfosStudentDTO> getStudentByProgram(ProgramID programID);

    @Transactional
    ResponseEntity<NewAdminDTO> saveAdmin(@NotNull NewAdminDTO newAdminDTO);

    @Transactional
    ResponseEntity<NewStudentDTO> saveStudent(@NotNull NewStudentDTO studentDTO);

    @Transactional
    ResponseEntity<String> changePW(@NotNull ChangePWDTO pwDTO);

    @Transactional
    ResponseEntity<String> resetPW(String targetUserEmail);

    List<InfosAdminDTO> getAdmins();

    Page<InfosAdminDTO> getAdminsByCriteria(String email, String firstName, String lastName, DepartmentName departmentName, int page, int size);

    Page<InfosStudentDTO> getStudentsByCriteriaAsAdmin(String email, String firstName, String lastName, ProgramID programID, String code, int page, int size);

    ResponseEntity<String> registerStudent(NewPendingStudentDTO pendingStudentDTO);

    Page<PendingStudent> getPendingStudent(String email, int page, int size);

    @Transactional
    ResponseEntity<?> approvingStudentRegistration(@NotNull String email);

    @Transactional
    ResponseEntity<String> declineStudentRegistration(@NotNull String email);

    @Transactional
    ResponseEntity<String> toggleEnableUserAccount(String email);

    @Transactional
    ResponseEntity<String> banStudentRegistration(@NotNull String email);

    ResponseEntity<InfosStudentDTO> updateStudentInfo(@NotNull InfosStudentDTO studentDTO);

    Map<ProgramID, List<Double>> getProgramIdCounts();

    ResponseEntity<PendingStudent> getPendingStudentByEmail(String email);

    @Transactional
    ResponseEntity<String> approveMultipleRegistrations(List<String> emails);

    @Transactional
    ResponseEntity<String> banMultipleRegistrations(List<String> emails);

    @Transactional
    void declineMultipleRegistrations(List<String> emails);

    @Transactional
    ResponseEntity<String> deleteMultipleUsers(List<String> emails);

    ResponseEntity<String> resetPasswordToMultipleUsers(List<String> emails);

    void toggleUserAccount(List<String> emails);
}
