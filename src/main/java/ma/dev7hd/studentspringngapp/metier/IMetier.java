package ma.dev7hd.studentspringngapp.metier;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfoStatusChangesDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewAdminDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.dtos.otherDTOs.ChangePWDTO;
import ma.dev7hd.studentspringngapp.entities.Payment;
import ma.dev7hd.studentspringngapp.entities.User;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IMetier {
    ResponseEntity<Payment> saveNewPayment(NewPaymentDTO newPaymentDTO,
                                           @org.jetbrains.annotations.NotNull MultipartFile file) throws IOException;

    ResponseEntity<InfoPaymentDTO> updatePaymentStatus(UUID id, PaymentStatus status);

    byte[] getReceipt(UUID paymentId) throws IOException;

    ResponseEntity<User> deleteUser(String email);

    List<InfoPaymentDTO> findAllPayments();

    List<InfoPaymentDTO> getPaymentsByStatus(PaymentStatus status);

    List<InfoPaymentDTO> getPaymentsByType(PaymentType type);

    InfoPaymentDTO getPaymentById(UUID paymentId);

    ResponseEntity<List<InfoPaymentDTO>> getStudentPayments(String code);

    List<InfosStudentDTO> getAllStudents();

    ResponseEntity<InfosStudentDTO> getStudentByCode(String code);

    ResponseEntity<InfosStudentDTO> getStudentById(String email);

    List<InfosStudentDTO> getStudentByProgram(ProgramID programID);

    ResponseEntity<NewAdminDTO> saveAdmin(@NotNull NewAdminDTO adminDTO);

    ResponseEntity<NewStudentDTO> saveStudent(@NotNull NewStudentDTO studentDTO);

    ResponseEntity<String> changePW(@NotNull ChangePWDTO changePWDTO);

    ResponseEntity<String> resetPW(String userEmail);

    List<InfoStatusChangesDTO> getChanges();

    List<InfosAdminDTO> getAdmins();

    void emptyBlacklistTokens();

    Page<InfosAdminDTO> getAdminsByCriteria(String email, String firstName, String lastName, DepartmentName departmentName,int page, int size);

    Page<InfosStudentDTO> getStudentsByCriteria(String email, String firstName, String lastName, ProgramID programID, String code, int page, int size);

    Page<InfoPaymentDTO> getPaymentsByCriteria(String code, Double min, Double max, PaymentStatus status, PaymentType type, int page, int size);
}
