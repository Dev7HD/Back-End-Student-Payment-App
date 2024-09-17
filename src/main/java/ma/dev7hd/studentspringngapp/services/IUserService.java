package ma.dev7hd.studentspringngapp.services;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.entities.PendingStudent;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IUserService {
    ResponseEntity<String> deleteUserByEmail(String email);

    List<InfosStudentDTO> getAllStudents();

    List<InfosStudentDTO> getStudentByProgram(ProgramID programID);

    ResponseEntity<InfosStudentDTO> getStudentByCode(String code);

    ResponseEntity<NewAdminDTO> addAdmin(NewAdminDTO adminDTO);

    ResponseEntity<NewStudentDTO> addStudent(NewStudentDTO studentDTO);

    ResponseEntity<InfosStudentDTO> updateStudent(InfosStudentDTO studentDTO);

    ResponseEntity<String> changePassword(ChangePWDTO changePWDTO);

    List<InfosAdminDTO> getAllAdmins();

    ResponseEntity<String> resetPW(String email);

    ResponseEntity<InfosStudentDTO> getStudentById(String email);

    Page<InfosAdminDTO> getAdminsByCriteria(String email, String firstName, String lastName, DepartmentName departmentName, int page, int size);

    Page<InfosStudentDTO> getStudentsByCriteria(String email, String firstName, String lastName, ProgramID programID, String code, int page, int size);

    ResponseEntity<String> registerStudent(@NotNull NewPendingStudentDTO pendingStudentDTO);

    ResponseEntity<?> approvingStudentRegistration(@NotNull String email);

    ResponseEntity<String> declineStudentRegistration(@NotNull String email);

    ResponseEntity<String> banStudentRegistration(@NotNull String email);

    Map<ProgramID, List<Double>> getProgramIdCounts();

    ResponseEntity<String> toggleEnableUserAccount(String email);

    Page<PendingStudent> getPendingStudent(String email, Boolean isSeen, int page, int size);

    void onLoginNotifications();

    ResponseEntity<PendingStudent> getPendingStudentByEmail(String email);

    void markAsReadAllPendingStudents();

    ResponseEntity<String> uploadStudentFile(@NotNull MultipartFile file) throws Exception;

    void deleteStudentRegistrationNotification(String email);
}
