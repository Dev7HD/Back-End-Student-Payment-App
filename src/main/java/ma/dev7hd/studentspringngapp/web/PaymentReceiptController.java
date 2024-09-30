package ma.dev7hd.studentspringngapp.web;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InvoiceDTO;
import ma.dev7hd.studentspringngapp.services.generateReceipt.PaymentReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class PaymentReceiptController {
    private PaymentReceiptService paymentReceiptService;

    @GetMapping("/receipt/generate-receipt")
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
}
