package ma.dev7hd.studentspringngapp.web;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.*;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewPaymentDTO;
import ma.dev7hd.studentspringngapp.enumirat.Months;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import ma.dev7hd.studentspringngapp.services.IPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/payments")
public class PaymentRestController {
    private final IPaymentService iPaymentService;

    /**
     * Get all payments
     * @return List<Payment>
     */
    @GetMapping(path = "/all")
    //@PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN')")
    public List<InfoPaymentDTO> allPayments(){
        return iPaymentService.getAllPayments();
    }

    /**
     * Get all student payments by student code
     * @param code is a student code
     * @return List<Payment>
     */
    @GetMapping(path = "/student/{code}")
    //@PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<InfoPaymentDTO>> allStudentPayments(@PathVariable String code) {
        return iPaymentService.getStudentPayments(code);
    }

    /**
     * Get the payment by its id
     * @param id is the payment id
     * @return Payment or null if doesn't exist
     */
    @GetMapping(path = "/{id}")
    //@PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<InfoPaymentDTO> paymentById(@PathVariable UUID id) {
        return iPaymentService.getPaymentById(id);
    }

    /**
     * Get all payments by the status
     * @param paymentStatus is the payment status
     * @return List<Payment>
     */
    @GetMapping(path = "/status/{status}")
    //@PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN')")
    public List<InfoPaymentDTO> paymentByStatus(@PathVariable(name = "status") PaymentStatus paymentStatus) {
        return iPaymentService.getPaymentsByStatus(paymentStatus);
    }

    /**
     * Get all payments knowing the type
     * @param paymentType is the payment type
     * @return List<Payment>
     */
    @GetMapping(path = "/type/{type}")
    //@PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN')")
    public List<InfoPaymentDTO> paymentByType(@PathVariable(name = "type") PaymentType paymentType) {
        return iPaymentService.getPaymentsByType(paymentType);
    }

    /**
     * Update the payment status
     * @param id is payment id
     * @param newStatus is the new status
     * @return Optional<Payment>
     */
    @PatchMapping("/{id}/status-update")
    public ResponseEntity<InfoPaymentDTO> paymentStatusUpdate(@PathVariable UUID id, @RequestParam PaymentStatus newStatus) {
        return iPaymentService.paymentStatusUpdate(id,newStatus);
    }

    /**
     * Add new payment
     * @param newPaymentDTO is the object that contains the payment information
     * @param file is the payment receipt
     * @return ResponseEntity<Payment>
     * @throws IOException in case exception on uploading receipt
     */
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InfoSavedPayment> addNewPayment(NewPaymentDTO newPaymentDTO, @Parameter(description = "PDF to upload") @RequestPart(value = "file")MultipartFile file) throws IOException {
        System.out.println("New Payment");
        return iPaymentService.newPayment(newPaymentDTO,file);
    }

    /**
     * Get the payment file
     * @param paymentId is the payment id
     * @return byte[]
     * @throws IOException in case exception on reading receipt
     */
    @GetMapping(path = "/receipt/{paymentId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] getPaymentFile(@PathVariable UUID paymentId) throws IOException {
        return iPaymentService.getPaymentFile(paymentId);
    }

    /**
     * Get a list of the status that was changed, the date and the admins that make this changes
     * @return List<InfoStatusChangesDTO>
     */
    @GetMapping("/changes")
    public List<InfoStatusChangesDTO> changes() {
        return iPaymentService.getPaymentStatusChanges();
    }

    /**
     * Get all payments with backend pagination and multi-criteria filtering for admin user
     * @param paymentType PaymentType
     * @param paymentStatus PaymentStatus
     * @param code String
     * @param email String
     * @param min int
     * @param max int
     * @param page default 0
     * @param size default 10
     * @return Page<InfoPaymentDTO>
     */
    @GetMapping("/filter")
    public Page<InfoAdminPaymentDTO> getPaymentsByCriteriaAsAdmin(@RequestParam(name = "type", defaultValue = "") PaymentType paymentType,
                                                                @RequestParam(name = "status", defaultValue = "") PaymentStatus paymentStatus,
                                                                @RequestParam(defaultValue = "") String code,
                                                                @RequestParam(defaultValue = "") String email,
                                                                @RequestParam(defaultValue = "") Double min,
                                                                @RequestParam(defaultValue = "") Double max,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        return iPaymentService.getPaymentsByCriteriaAsAdmin(email, code, min, max, paymentStatus, paymentType, page, size);
    }

    /**
     * Get all payments with backend pagination and multi-criteria filtering for student user
     * @param paymentType PaymentType
     * @param paymentStatus PaymentStatus
     * @param min int
     * @param max int
     * @param page default 0
     * @param size default 10
     * @return Page<InfoPaymentDTO>
     */
    @GetMapping("/student-payments")
    public Page<InfoStudentPaymentDTO> getPaymentsByCriteriaAsStudent(@RequestParam(name = "type", defaultValue = "") PaymentType paymentType,
                                                                      @RequestParam(name = "status", defaultValue = "") PaymentStatus paymentStatus,
                                                                      @RequestParam(defaultValue = "") Double min,
                                                                      @RequestParam(defaultValue = "") Double max,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size) {
        return iPaymentService.getPaymentsByCriteriaAsStudent(min, max, paymentStatus, paymentType, page, size);
    }

    @GetMapping("/statistics-by-mount")
    public ResponseEntity<Map<Months, Long>> getPaymentsByMonth(@RequestParam(required = false) Integer month) {
        return iPaymentService.getPaymentsByMonth(month);
    }
}
