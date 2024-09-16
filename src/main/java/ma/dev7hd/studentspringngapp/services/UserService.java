package ma.dev7hd.studentspringngapp.services;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPendingStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.entities.PendingStudent;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.metier.dataFromFile.ILoadStudents;
import ma.dev7hd.studentspringngapp.metier.user.IUserMetier;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@AllArgsConstructor
public class UserService implements IUserService {
    private final IUserMetier userMetier;
    private final ILoadStudents processData;

    @Override
    public ResponseEntity<String> deleteUserByEmail(String email) {
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
    public ResponseEntity<InfosStudentDTO> updateStudent(InfosStudentDTO studentDTO) {
        return userMetier.updateStudentInfo(studentDTO);
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
    public Map<ProgramID, List<Double>> getProgramIdCounts(){
        return userMetier.getProgramIdCounts();
    }

    @Override
    public ResponseEntity<String> toggleEnableUserAccount(String email){
        return userMetier.toggleEnableUserAccount(email);
    }

    @Override
    public Page<PendingStudent> getPendingStudent(String email, Boolean isSeen, int page, int size){
        return userMetier.getPendingStudent(email, isSeen, page, size);
    }

    @Override
    public void onLoginNotifications(){
        userMetier.onLoginNotifications();
    }

    @Override
    public ResponseEntity<PendingStudent> getPendingStudentByEmail(String email){
        return userMetier.getPendingStudentByEmail(email);
    }

    @Override
    public void markAsReadAllPendingStudents(){
        userMetier.markAsReadAllPendingStudents();
    }

    @Override
    public ResponseEntity<String> uploadStudentFile(@NotNull MultipartFile file) throws Exception {
        return processData.uploadStudentFile(file);
    }
}
