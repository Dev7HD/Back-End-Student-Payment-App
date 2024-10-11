package ma.dev7hd.studentspringngapp.services.generatePDF.profile;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.ProfileDTO;
import ma.dev7hd.studentspringngapp.entities.payments.Payment;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.services.global.IUserDataProvider;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProfileService implements IProfileService {

    private final SpringTemplateEngine templateEngine;
    private final IUserDataProvider iUserDataProvider;

    @Override
    public ProfileDTO generateProfile() throws IOException {
        Optional<Student> optionalCurrentStudent = iUserDataProvider.getCurrentStudent();

        if (optionalCurrentStudent.isPresent() && optionalCurrentStudent.get().getPhoto() != null) {
            Student student = optionalCurrentStudent.get();

            String photoUri = student.getPhoto();

            byte[] photo = Files.readAllBytes(Paths.get(URI.create(photoUri)));

            // 64 base logo
            String studentPhoto = get64BaseImg(photo);

            double total = 0;

            List<Payment> payments = student.getPayments();

            List<Payment> validatedPayments = payments.stream().filter(p -> p.getStatus() == PaymentStatus.VALIDATED).toList();

            for (Payment payment : validatedPayments) {
                total += payment.getAmount();
            }

            // Prepare the Thymeleaf context with variables
            Context context = new Context();
            context.setVariable("studentCode", student.getCode());
            context.setVariable("payments", validatedPayments);
            context.setVariable("email", student.getEmail());
            context.setVariable("studentFName", student.getFirstName());
            context.setVariable("studentLName", student.getLastName());
            context.setVariable("studentProgram", student.getProgramId());
            context.setVariable("photo", studentPhoto);
            context.setVariable("total", total);


            // Render the HTML template as a String
            String htmlContent = templateEngine.process("profile", context);

            // Generate PDF from the HTML content
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(htmlContent);
                renderer.layout();
                renderer.createPDF(outputStream);

                ProfileDTO profileDTO = new ProfileDTO();
                profileDTO.setStream(new ByteArrayInputStream(outputStream.toByteArray()));
                profileDTO.setPdfName(student.getCode() + "_Profile.pdf");

                return profileDTO;

            } catch (Exception e) {
                throw new RuntimeException("Failed to generate PDF", e);
            }
        }
        return null;
    }

    private String get64BaseImg(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }

}
