package ma.dev7hd.studentspringngapp.services.generateReceipt;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.payments.Payment;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.repositories.payments.PaymentRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentReceiptService {

    private final SpringTemplateEngine templateEngine;
    private final PaymentRepository paymentRepository;

    public ByteArrayInputStream generatePaymentReceipt(UUID paymentId) throws IOException {

        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);

        if (optionalPayment.isPresent() && optionalPayment.get().getStatus() == PaymentStatus.VALIDATED){
            Payment payment = optionalPayment.get();

            if (payment.getInvoiceNumber() == null) {
                String generatedInvoiceNumber = generateInvoiceNumber(payment);
                payment.setInvoiceNumber(generatedInvoiceNumber);
                paymentRepository.save(payment); // Persist the invoice number
            }

            // 64 base logo
            String logoPNG = get64BaseImg("static/img/wallet.png");

            //64 base stamp
            String stamp = get64BaseImg("static/img/stamp.png");

            // Prepare the Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("invoiceNumber", payment.getInvoiceNumber());
            context.setVariable("studentCode", payment.getStudent().getCode());
            context.setVariable("paymentId", paymentId);
            context.setVariable("amount", payment.getAmount());
            context.setVariable("paymentDate", payment.getDate());
            context.setVariable("downloadDate", new Date());
            context.setVariable("studentFName", payment.getStudent().getFirstName());
            context.setVariable("studentLName", payment.getStudent().getLastName());
            context.setVariable("studentProgram", payment.getStudent().getProgramId());
            context.setVariable("paymentMethod", payment.getType());
            context.setVariable("logo", logoPNG);
            context.setVariable("stamp", stamp);


            // Render the HTML template as a String
            String htmlContent = templateEngine.process("receipt", context);

            // Generate PDF from the HTML content
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(htmlContent);
                renderer.layout();
                renderer.createPDF(outputStream);

                return new ByteArrayInputStream(outputStream.toByteArray());
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate PDF", e);
            }
        }
        return null;
    }

    private String get64BaseImg(String classpath) throws IOException {
        ClassPathResource imgFile = new ClassPathResource(classpath);
        Path imagePath = Paths.get(imgFile.getURI());
        byte[] imageBytes = Files.readAllBytes(imagePath);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String generateInvoiceNumber(Payment payment) {
        // For example: use current year + payment ID hash as the invoice number
        return "INV-" + Year.now().getValue() + "-" + payment.getId().toString().substring(0, 8);
    }
}
