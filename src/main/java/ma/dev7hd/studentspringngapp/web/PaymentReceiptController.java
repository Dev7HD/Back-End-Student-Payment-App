package ma.dev7hd.studentspringngapp.web;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.services.generateReceipt.PaymentReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class PaymentReceiptController {
    private PaymentReceiptService paymentReceiptService;

    @GetMapping("/receipt/generate-receipt")
    public ResponseEntity<?> generatePaymentReceipt(UUID paymentId) throws IOException {

        // Generate the PDF receipt
        ByteArrayInputStream receiptStream = paymentReceiptService.generatePaymentReceipt(paymentId);

        if (receiptStream != null) {
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=payment_receipt.pdf");

            return new ResponseEntity<>(receiptStream.readAllBytes(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.badRequest().body("The payment must be 'VALIDATED' to be downloaded");
        }

    }
}
