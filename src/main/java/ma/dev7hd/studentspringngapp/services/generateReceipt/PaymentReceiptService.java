package ma.dev7hd.studentspringngapp.services.generateReceipt;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.payments.Payment;
import ma.dev7hd.studentspringngapp.repositories.payments.PaymentRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentReceiptService {

    private final SpringTemplateEngine templateEngine;
    private final PaymentRepository paymentRepository;

    public ByteArrayInputStream generatePaymentReceipt(UUID paymentId) {

        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);

        if (optionalPayment.isPresent()){
            Payment payment = optionalPayment.get();

            // Prepare the Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("studentCode", payment.getStudent().getCode());
            context.setVariable("paymentId", paymentId);
            context.setVariable("amount", payment.getAmount());
            context.setVariable("paymentDate", payment.getDate());
            context.setVariable("downloadDate", new Date());
            context.setVariable("studentFName", payment.getStudent().getFirstName());
            context.setVariable("studentLName", payment.getStudent().getLastName());
            context.setVariable("studentProgram", payment.getStudent().getProgramId());
            context.setVariable("paymentMethod", payment.getType());


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
}
