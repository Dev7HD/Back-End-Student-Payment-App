package ma.dev7hd.studentspringngapp.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.entities.User;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.metier.IMetier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class UserService implements IUserService {
    private final IMetier iMetier;

    @Override
    public ResponseEntity<User> deleteUserByEmail(String email) {
        return iMetier.deleteUser(email);
    }

    @Override
    public List<InfosStudentDTO> getAllStudents() {
        return iMetier.getAllStudents();
    }

    @Override
    public List<InfosStudentDTO> getStudentByProgram(ProgramID programID){
        return iMetier.getStudentByProgram(programID);
    }

    @Override
    public ResponseEntity<InfosStudentDTO> getStudentByCode(String code){
        return iMetier.getStudentByCode(code);
    }

    @Override
    public ResponseEntity<NewAdminDTO> addAdmin(NewAdminDTO adminDTO) {
        return iMetier.saveAdmin(adminDTO);
    }

    @Override
    public ResponseEntity<NewStudentDTO> addStudent(NewStudentDTO studentDTO) {
        return iMetier.saveStudent(studentDTO);
    }

    @Override
    public ResponseEntity<String> changePassword(ChangePWDTO changePWDTO) {
        return iMetier.changePW(changePWDTO);
    }

    @Override
    public List<InfosAdminDTO> getAllAdmins() {
        return iMetier.getAdmins();
    }

    @Override
    public ResponseEntity<String> resetPW(String email){
        return iMetier.resetPW(email);
    }

    @Override
    public ResponseEntity<InfosStudentDTO> getStudentById(String email) {
        return iMetier.getStudentById(email);
    }

    @Scheduled(fixedRate = 86400000)
    @Override
    public void emptyBlackListedTokens(){
        iMetier.emptyBlacklistTokens();
    }
}
