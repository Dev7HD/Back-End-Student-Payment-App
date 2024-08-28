package ma.dev7hd.studentspringngapp.services;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.entities.User;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public interface IUserService {
    ResponseEntity<User> deleteUserByEmail(String email);

    List<InfosStudentDTO> getAllStudents();

    List<InfosStudentDTO> getStudentByProgram(ProgramID programID);

    ResponseEntity<InfosStudentDTO> getStudentByCode(String code);

    ResponseEntity<NewAdminDTO> addAdmin(NewAdminDTO adminDTO);

    ResponseEntity<NewStudentDTO> addStudent(NewStudentDTO studentDTO);

    ResponseEntity<String> changePassword(ChangePWDTO changePWDTO);

    List<InfosAdminDTO> getAllAdmins();

    ResponseEntity<String> resetPW(String email);

    ResponseEntity<InfosStudentDTO> getStudentById(String email);

    @Scheduled(fixedRate = 86400000)
    void emptyBlackListedTokens();
}
