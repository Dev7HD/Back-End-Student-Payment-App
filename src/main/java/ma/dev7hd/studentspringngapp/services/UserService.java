package ma.dev7hd.studentspringngapp.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.entities.PendingStudent;
import ma.dev7hd.studentspringngapp.entities.User;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.metier.student.IUserMetier;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class UserService implements IUserService {
    private final IUserMetier userMetier;

    @Override
    public ResponseEntity<User> deleteUserByEmail(String email) {
        return userMetier.deleteUser(email);
    }

    @Override
    public List<InfosStudentDTO> getAllStudents() {
        return userMetier.getAllStudents();
    }

    @Override
    public List<InfosStudentDTO> getStudentByProgram(ProgramID programID){
        return userMetier.getStudentByProgram(programID);
    }

    @Override
    public ResponseEntity<InfosStudentDTO> getStudentByCode(String code){
        return userMetier.getStudentByCode(code);
    }

    @Override
    public ResponseEntity<NewAdminDTO> addAdmin(NewAdminDTO adminDTO) {
        return userMetier.saveAdmin(adminDTO);
    }

    @Override
    public ResponseEntity<NewStudentDTO> addStudent(NewStudentDTO studentDTO) {
        return userMetier.saveStudent(studentDTO);
    }

    @Override
    public ResponseEntity<String> changePassword(ChangePWDTO changePWDTO) {
        return userMetier.changePW(changePWDTO);
    }

    @Override
    public List<InfosAdminDTO> getAllAdmins() {
        return userMetier.getAdmins();
    }

    @Override
    public ResponseEntity<String> resetPW(String email){
        return userMetier.resetPW(email);
    }

    @Override
    public ResponseEntity<InfosStudentDTO> getStudentById(String email) {
        return userMetier.getStudentById(email);
    }

    @Override
    public Page<InfosAdminDTO> getAdminsByCriteria(String email, String firstName, String lastName, DepartmentName departmentName, int page, int size){
        return userMetier.getAdminsByCriteria(email, firstName, lastName, departmentName, page, size);
    }

    @Override
    public Page<InfosStudentDTO> getStudentsByCriteria(String email, String firstName, String lastName, ProgramID programID, String code, int page, int size){
        return userMetier.getStudentsByCriteriaAsAdmin(email, firstName, lastName, programID, code, page, size);
    }

    @Override
    public ResponseEntity<String> registerStudent(@NotNull NewPendingStudentDTO pendingStudentDTO){
        return userMetier.registerStudent(pendingStudentDTO);
    }

    @Override
    public ResponseEntity<?> approvingStudentRegistration(@NotNull String email){
        return userMetier.approvingStudentRegistration(email);
    }

    @Override
    public ResponseEntity<String> declineStudentRegistration(@NotNull String email){
        return userMetier.declineStudentRegistration(email);
    }

    @Override
    public ResponseEntity<String> banStudentRegistration(@NotNull String email){
        return userMetier.banStudentRegistration(email);
    }

    @Override
    public List<PendingStudent> getPendingStudent(){
        return userMetier.getPendingStudents();
    }

    @Override
    public ResponseEntity<String> expireUserCredentials(String email){
        return userMetier.expireUserCredentials(email);
    }

    @Override
    public ResponseEntity<String> lockUserAccount(String email){
        return userMetier.lockUserAccount(email);
    }

    @Override
    public ResponseEntity<String> disableUserAccount(String email){
        return userMetier.disableUserAccount(email);
    }

    @Override
    public ResponseEntity<String> unlockUserAccount(String email){
        return userMetier.unlockUserAccount(email);
    }

    @Override
    public ResponseEntity<String> enableUserAccount(String email){
        return userMetier.enableUserAccount(email);
    }
}
