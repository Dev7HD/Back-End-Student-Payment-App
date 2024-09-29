package ma.dev7hd.studentspringngapp.services.global;

import ma.dev7hd.studentspringngapp.entities.users.Admin;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.entities.users.User;

import java.util.Optional;

public interface IUserDataProvider {
    Optional<Admin> getCurrentAdmin();

    String getCurrentUserEmail();

    User getUserByEmail(String email);

    Optional<User> getCurrentUser();

    Optional<Student> getCurrentStudent();

    Optional<Student> getStudentByEmail(String email);

    Optional<Admin> getAdminByEmail(String email);
}
