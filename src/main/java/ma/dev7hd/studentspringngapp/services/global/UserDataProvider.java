package ma.dev7hd.studentspringngapp.services.global;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.users.Admin;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.entities.users.User;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.users.AdminRepository;
import ma.dev7hd.studentspringngapp.repositories.users.StudentRepository;
import ma.dev7hd.studentspringngapp.repositories.users.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Optional;
import java.util.Random;

@Component
@AllArgsConstructor
public class UserDataProvider implements IUserDataProvider {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;

    @Override
    public Optional<Admin> getCurrentAdmin(){
        return adminRepository.findById(getCurrentUserEmail());
    }

    @Override
    public String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findById(email).orElse(null);
    }

    @Override
    public Optional<User> getCurrentUser() {
        String userEmail = getCurrentUserEmail();
        return userRepository.findByEmail(userEmail);
    }

    @Override
    public Optional<Student> getCurrentStudent(){
        String currentUserEmail = getCurrentUserEmail();
        return studentRepository.findById(currentUserEmail);
    }

    @Override
    public Optional<Student> getStudentByEmail(String email){
        return studentRepository.findById(email);
    }

    @Override
    public Optional<Admin> getAdminByEmail(String email){
        return adminRepository.findById(email);
    }

    @Override
    public String generateStudentCode(ProgramID program, Integer i) {
        StringBuilder studentCode = new StringBuilder("STU");

        // Append the last two digits of the current year
        int year = Year.now().getValue() % 100;
        studentCode.append(year);

        // Append the program-specific code
        switch (program) {
            case SMP:
                studentCode.append("13");
                break;
            case SMC:
                studentCode.append("12");
                break;
            case SMA:
                studentCode.append("10");
                break;
            case SMI:
                studentCode.append("11");
                break;
            case SVT:
                studentCode.append("14");
                break;
            default:
                throw new IllegalArgumentException("Invalid Program");
        }

        // Append a unique 4-digit number
        if (i != null) return generateUniqueCode(studentCode.toString(), i);

        return generateUniqueCode(studentCode.toString());
    }

    private String generateUniqueCode(String prefix, Integer i) {
        return prefix + i;
    }

    private String generateUniqueCode(String prefix) {
        Random random = new Random();
        String studentCode;
        do {
            int randomDigits = 1000 + random.nextInt(9000); // Generate a 4-digit number
            studentCode = prefix + randomDigits;
        } while (studentRepository.existsByCode(studentCode)); // Check if code is unique

        return studentCode;
    }
}
