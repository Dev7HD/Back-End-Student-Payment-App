package ma.dev7hd.studentspringngapp;

import io.github.cdimascio.dotenv.Dotenv;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.payments.PaymentRepository;
import ma.dev7hd.studentspringngapp.repositories.users.StudentRepository;
import ma.dev7hd.studentspringngapp.repositories.users.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@EnableScheduling
@SpringBootApplication
public class StudentSpringNgAppApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        System.setProperty("DB_URL", Objects.requireNonNull(dotenv.get("DB_URL")));
        System.setProperty("DB_USERNAME", Objects.requireNonNull(dotenv.get("DB_USERNAME")));
        System.setProperty("DB_PASSWORD", Objects.requireNonNull(dotenv.get("DB_PASSWORD")));

        SpringApplication.run(StudentSpringNgAppApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    CommandLineRunner commandLineRunner(StudentRepository studentRepository, UserRepository userRepository, PaymentRepository paymentRepository) {
        return args -> {
            Integer total = 0;
            for (ProgramID programID : ProgramID.values()) {
                Integer count = studentRepository.countByProgramId(programID);
                total += count;
            }
            Student.totalStudents = total;
            for (ProgramID programID : ProgramID.values()) {
                Integer count = studentRepository.countByProgramId(programID);
                List<Double> values = Arrays.asList(count.doubleValue(), count.doubleValue() / total.doubleValue());
                Student.programIDCounter.put(programID, values);
            }

            /*final Path PAYMENTS_FOLDER_PATH = Paths.get(System.getProperty("user.home"), "data", "payments");
            Path filePath = PAYMENTS_FOLDER_PATH.resolve("p.pdf");
            File file = new File(filePath.toUri());
            URI uri = file.toURI();
            Student student1 = new Student();
            student1.setFirstName("John");
            student1.setLastName("Doe");
            student1.setEmail("john@doe.com");
            student1.setPassword("$2y$10$fxUIQ9WtSfDTvahOsA.NJO55lClLZObHvRZ1SC0XlU9KCuBRa6LdO");
            student1.setCode("123456789");
            student1.setProgramId(ProgramID.SMA);
            student1.setEnabled(true);
            userRepository.save(student1);

            Admin admin = new Admin();
            admin.setFirstName("Hamza");
            admin.setLastName("Damiri");
            admin.setEmail("hamza@damiri.com");
            admin.setPassword("$2y$10$fxUIQ9WtSfDTvahOsA.NJO55lClLZObHvRZ1SC0XlU9KCuBRa6LdO");
            admin.setDepartmentName(DepartmentName.PHYSICS);
            admin.setEnabled(true);
            userRepository.save(admin);

            studentRepository.findAll().forEach(student -> {
                for (int i = 0; i < 12; i++) {
                    double random = Math.random();
                    Payment payment = Payment.builder()
                            .amount((int) (random * 10000))
                            .student(student1)
                            .registerDate(new Date())
                            .date(new Date(2024, i, (int) (Math.random() * 30), (int) (Math.random() * 24), (int) (Math.random() * 60), (int) (Math.random() * 60)))
                            .type(random >= 0.75 ? PaymentType.CASH : random >= 0.5 ? PaymentType.CHECK : random >= 0.25 ? PaymentType.DEPOSIT : PaymentType.TRANSFER)
                            .status(random >= 0.66 ? PaymentStatus.VALIDATED : random >= 0.33 ? PaymentStatus.CREATED : PaymentStatus.REJECTED )
                            .addedBy(student)
                            .receipt(uri.toString()).build();
                    paymentRepository.save(payment);
                }
            });*/
        };
    }
}
