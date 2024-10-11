package ma.dev7hd.studentspringngapp.web;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.ProfileDTO;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InvoiceDTO;
import ma.dev7hd.studentspringngapp.services.generatePDF.profile.IProfileService;
import ma.dev7hd.studentspringngapp.services.generatePDF.receipt.IPaymentReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/generate-pdf")
public class GeneratePDFController {
    private IPaymentReceiptService paymentReceiptService;
    private IProfileService profileService;

    @GetMapping("/receipt")
    public ResponseEntity<?> generatePaymentReceipt(UUID paymentId) throws IOException {

        // Generate the PDF receipt
        InvoiceDTO invoiceDTO = paymentReceiptService.generatePaymentReceipt(paymentId);

        if (invoiceDTO != null) {
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=" + invoiceDTO.getNumber() +".pdf");

            return new ResponseEntity<>(invoiceDTO.getStream().readAllBytes(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.badRequest().body("The payment must be 'VALIDATED' to be downloaded");
        }

    }

    @GetMapping("/profile")
    public ResponseEntity<?> generateProfile() throws IOException {
        ProfileDTO profileDTO = profileService.generateProfile();

        if (profileDTO != null) {
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=" + profileDTO.getPdfName());

            return new ResponseEntity<>(profileDTO.getStream().readAllBytes(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.badRequest().body("Error generating profile.");
        }
    }
}
