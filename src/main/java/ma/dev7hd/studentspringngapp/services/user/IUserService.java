package ma.dev7hd.studentspringngapp.services.user;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.entities.PendingStudent;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IUserService {
    ResponseEntity<String> deleteUserByEmail(String email);

    List<InfosStudentDTO> getAllStudents();

    ResponseEntity<InfosStudentDTO> getStudentByCode(String code);

    ResponseEntity<InfosStudentDTO> getStudentById(String email);

    List<InfosStudentDTO> getStudentByProgram(ProgramID programID);

    ResponseEntity<NewAdminDTO> saveAdmin(@NotNull NewAdminDTO newAdminDTO);

    ResponseEntity<NewStudentDTO> saveStudent(@NotNull NewStudentDTO studentDTO);

    ResponseEntity<String> changePW(@NotNull ChangePWDTO pwDTO);

    ResponseEntity<String> resetPW(String targetUserEmail);

    List<InfosAdminDTO> getAdmins();

    Page<InfosAdminDTO> getAdminsByCriteria(String email, String firstName, String lastName, DepartmentName departmentName, int page, int size);

    Page<InfosStudentDTO> getStudentsByCriteriaAsAdmin(String email, String firstName, String lastName, ProgramID programID, String code, int page, int size);

    ResponseEntity<String> registerStudent(NewPendingStudentDTO pendingStudentDTO);

    Page<PendingStudent> getPendingStudent(String email, int page, int size);


    ResponseEntity<?> approvingStudentRegistration(@NotNull String email);

    ResponseEntity<String> declineStudentRegistration(@NotNull String email);


    ResponseEntity<String> toggleEnableUserAccount(String email);

    ResponseEntity<String> banStudentRegistration(@NotNull String email);

    ResponseEntity<InfosStudentDTO> updateStudentInfo(@NotNull InfosStudentDTO studentDTO);

    Map<ProgramID, List<Double>> getProgramIdCounts();

    ResponseEntity<PendingStudent> getPendingStudentByEmail(String email);
}
