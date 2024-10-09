package ma.dev7hd.studentspringngapp.services.user;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.PictureDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.entities.registrations.PendingStudent;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IUserService {

    @Transactional
    ResponseEntity<String> deleteUserByEmail(String email);

    @Transactional
    InfosAdminDTO updateAdmin(InfosAdminDTO adminDTO);

    @Transactional
    Boolean toggleAdminAccount(String email) throws ChangeSetPersister.NotFoundException;

    List<InfosStudentDTO> getAllStudents();

    ResponseEntity<InfosStudentDTO> getStudentByCode(String code);

    ResponseEntity<InfosStudentDTO> getStudentById(String email);

    List<InfosStudentDTO> getStudentByProgram(ProgramID programID);

    @Transactional
    ResponseEntity<NewAdminDTO> saveAdmin(@NotNull NewAdminDTO newAdminDTO);

    @Transactional
    ResponseEntity<InfosStudentDTO> saveStudent(@NotNull NewStudentDTO studentDTO, MultipartFile photo) throws IOException;

    @Transactional
    ResponseEntity<String> changePW(@NotNull ChangePWDTO pwDTO);

    @Transactional
    ResponseEntity<String> resetPW(String targetUserEmail);

    List<InfosAdminDTO> getAdmins();

    Page<InfosAdminDTO> getAdminsByCriteria(String email, String firstName, String lastName, DepartmentName departmentName, int page, int size);

    Page<InfosStudentDTO> getStudentsByCriteriaAsAdmin(String email, String firstName, String lastName, ProgramID programID, String code, int page, int size);

    ResponseEntity<String> registerStudent(NewPendingStudentDTO pendingStudentDTO, MultipartFile photo) throws IOException;

    Page<PendingStudent> getPendingStudent(String email, int page, int size);

    @Transactional
    ResponseEntity<?> approvingStudentRegistration(@NotNull String email) throws IOException;

    @Transactional
    ResponseEntity<String> declineStudentRegistration(@NotNull String email);

    @Transactional
    ResponseEntity<String> toggleEnableUserAccount(String email);

    @Transactional
    ResponseEntity<String> banStudentRegistration(@NotNull String email);

    ResponseEntity<InfosStudentDTO> updateStudentInfo(@NotNull InfosStudentDTO studentDTO);

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

    PictureDTO getProfilePicture(String email) throws IOException;
}
