package ma.dev7hd.studentspringngapp.services.global;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.users.Admin;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.entities.users.User;
import ma.dev7hd.studentspringngapp.repositories.users.AdminRepository;
import ma.dev7hd.studentspringngapp.repositories.users.StudentRepository;
import ma.dev7hd.studentspringngapp.repositories.users.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
}
