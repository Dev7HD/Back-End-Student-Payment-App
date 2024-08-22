package ma.dev7hd.studentspringngapp;

import ma.dev7hd.studentspringngapp.entities.Admin;
import ma.dev7hd.studentspringngapp.entities.Payment;
import ma.dev7hd.studentspringngapp.entities.Student;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;
import ma.dev7hd.studentspringngapp.enumirat.PaymentType;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.PaymentRepository;
import ma.dev7hd.studentspringngapp.repositories.StudentRepository;
import ma.dev7hd.studentspringngapp.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class StudentSpringNgAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentSpringNgAppApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    CommandLineRunner commandLineRunner(StudentRepository studentRepository, PaymentRepository paymentRepository, UserRepository userRepository) {
        return args -> {
            Student student1 = new Student();
            student1.setFirstName("John");
            student1.setLastName("Doe");
            student1.setEmail("john@doe.com");
            student1.setPassword("$2y$10$fxUIQ9WtSfDTvahOsA.NJO55lClLZObHvRZ1SC0XlU9KCuBRa6LdO");
            student1.setCode("123456789");
            student1.setProgramId(ProgramID.SMA);
            userRepository.save(student1);

            Admin admin = new Admin();
            admin.setFirstName("Hamza");
            admin.setLastName("Damiri");
            admin.setEmail("hamza@damiri.com");
            admin.setPassword("$2y$10$fxUIQ9WtSfDTvahOsA.NJO55lClLZObHvRZ1SC0XlU9KCuBRa6LdO");
            admin.setDepartmentName(DepartmentName.SMP);
            userRepository.save(admin);

            studentRepository.findAll().forEach(student -> {
                for (int i = 0; i < 4; i++) {
                    double random = Math.random();
                    Payment payment = Payment.builder()
                            .amount((int) (random * 10000))
                            .student(student1)
                            .date(LocalDate.now())
                            .type(random >= 0.75 ? PaymentType.CASH : random >= 0.5 ? PaymentType.CHECK : random >= 0.25 ? PaymentType.DEPOSIT : PaymentType.TRANSFER)
                            .status(random >= 0.66 ? PaymentStatus.VALIDATED : random >= 0.33 ? PaymentStatus.CREATED : PaymentStatus.REJECTED )
                            .addedBy(student)
                            .receipt("./static/recipes").build();
                    paymentRepository.save(payment);
                }
            });
        };
    }

}
